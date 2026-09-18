package xyz.erupt.upms.service;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.EncryptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.util.TotpUtil;
import xyz.erupt.upms.vo.EruptMfaEnrollVo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Multi-factor authentication built on TOTP, see {@link TotpUtil}.
 *
 * <p>Two rules shape this service: a login token is minted only after the second factor
 * passes, and a secret only reaches the database once the user has proved they can read
 * codes from it. Everything half-finished lives in the session store with a short expiry.
 *
 * @author YuePeng
 * date 2026-09-18
 */
@Service
public class EruptMfaService {

    public static final int RECOVERY_CODE_COUNT = 10;

    private static final int RECOVERY_CODE_LENGTH = 10;

    private static final int TICKET_EXPIRE_MINUTES = 5;

    private static final int ENROLL_EXPIRE_MINUTES = 10;

    private static final int MAX_ATTEMPTS = 5;

    @Resource
    private EruptAppProp eruptAppProp;

    @Resource
    private EruptSessionService sessionService;

    @Resource
    private EruptDao eruptDao;

    @Value("${spring.application.name:Erupt}")
    private String applicationName;

    public boolean isEnable() {
        return eruptAppProp.getMfa().isEnable();
    }

    /**
     * Whether this user must present a one-time code before a session is issued.
     */
    public boolean isBound(EruptUser eruptUser) {
        return this.isEnable() && null != eruptUser
                && Boolean.TRUE.equals(eruptUser.getMfaEnabled())
                && StringUtils.isNotBlank(eruptUser.getMfaSecret());
    }

    // ------------------------------------------------------------------ login

    /**
     * Park a password-authenticated login and hand back a ticket. No session token exists yet.
     */
    public String issueTicket(EruptUser eruptUser) {
        String ticket = Erupts.generateCode(22);
        sessionService.put(SessionKey.MFA_TICKET + ticket, eruptUser.getId().toString(), TICKET_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return ticket;
    }

    /**
     * Resolve a ticket to its user, or null when it expired, never existed or was burnt.
     */
    public EruptUser findUserByTicket(String ticket) {
        if (StringUtils.isBlank(ticket)) return null;
        Object uid = sessionService.get(SessionKey.MFA_TICKET + ticket);
        if (null == uid) return null;
        return eruptDao.getEntityManager().find(EruptUser.class, Long.parseLong(uid.toString()));
    }

    public void burnTicket(String ticket) {
        sessionService.remove(SessionKey.MFA_TICKET + ticket);
        sessionService.remove(SessionKey.MFA_ERROR + ticket);
    }

    /**
     * Check a one-time code or a recovery code for a login attempt, counting failures
     * against the ticket so a stolen ticket cannot be brute forced.
     */
    @Transactional
    public void verifyForLogin(EruptUser eruptUser, String ticket, String code) {
        if (this.verify(eruptUser, code)) {
            this.burnTicket(ticket);
            return;
        }
        Long attempts = sessionService.increment(SessionKey.MFA_ERROR + ticket, TICKET_EXPIRE_MINUTES, TimeUnit.MINUTES);
        if (null != attempts && attempts >= MAX_ATTEMPTS) {
            this.burnTicket(ticket);
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.mfa.too_many_attempts"));
        }
        throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.mfa.code_error"));
    }

    /**
     * Accept either a time-based code or one of the stored recovery codes.
     * A recovery code is single use and is consumed here.
     */
    @Transactional
    public boolean verify(EruptUser eruptUser, String code) {
        if (null == eruptUser || StringUtils.isBlank(code)) return false;
        // re-read inside the transaction: the caller holds a detached entity whose
        // recovery code collection is lazy
        EruptUser user = eruptDao.getEntityManager().find(EruptUser.class, eruptUser.getId());
        if (null == user) return false;
        long counter = TotpUtil.verify(user.getMfaSecret(), code, eruptAppProp.getMfa().getWindow());
        if (counter >= 0) return this.markCounterUsed(user.getId(), counter);
        return this.consumeRecoveryCode(user, code);
    }

    /**
     * A code stays valid for the whole step, so remember the counters already spent and
     * refuse the second use. Returns false when this counter was already consumed.
     */
    private boolean markCounterUsed(Long uid, long counter) {
        String key = SessionKey.MFA_USED + uid + ":" + counter;
        if (sessionService.exist(key)) return false;
        long ttl = (long) (eruptAppProp.getMfa().getWindow() * 2 + 2) * TotpUtil.PERIOD;
        sessionService.put(key, "1", ttl, TimeUnit.SECONDS);
        return true;
    }

    private boolean consumeRecoveryCode(EruptUser eruptUser, String code) {
        if (null == eruptUser.getMfaRecoveryCodes() || eruptUser.getMfaRecoveryCodes().isEmpty()) return false;
        String hash = hashRecoveryCode(code);
        if (!eruptUser.getMfaRecoveryCodes().remove(hash)) return false;
        eruptDao.merge(eruptUser);
        return true;
    }

    // -------------------------------------------------------------- enrolment

    /**
     * Start enrolment: the secret lives in the session only, so an abandoned scan
     * never leaves a half-configured account behind.
     */
    public EruptMfaEnrollVo startEnroll(EruptUser eruptUser) {
        String secret = TotpUtil.generateSecret();
        sessionService.put(SessionKey.MFA_ENROLL + eruptUser.getId(), secret, ENROLL_EXPIRE_MINUTES, TimeUnit.MINUTES);
        EruptMfaEnrollVo vo = new EruptMfaEnrollVo();
        vo.setSecret(TotpUtil.humanize(secret));
        vo.setUri(TotpUtil.buildUri(this.issuer(), eruptUser.getAccount(), secret));
        return vo;
    }

    /**
     * Finish enrolment once the user echoes a code from the new secret,
     * and hand back the recovery codes, which are shown exactly once.
     */
    @Transactional
    public List<String> confirmEnroll(EruptUser eruptUser, String code) {
        Object secret = sessionService.get(SessionKey.MFA_ENROLL + eruptUser.getId());
        if (null == secret) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.mfa.enroll_expired"));
        if (TotpUtil.verify(secret.toString(), code, eruptAppProp.getMfa().getWindow()) < 0) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.mfa.code_error"));
        }
        EruptUser user = eruptDao.getEntityManager().find(EruptUser.class, eruptUser.getId());
        user.setMfaSecret(secret.toString());
        user.setMfaEnabled(true);
        List<String> codes = this.applyRecoveryCodes(user);
        eruptDao.merge(user);
        sessionService.remove(SessionKey.MFA_ENROLL + eruptUser.getId());
        return codes;
    }

    @Transactional
    public List<String> regenerateRecoveryCodes(EruptUser eruptUser) {
        EruptUser user = eruptDao.getEntityManager().find(EruptUser.class, eruptUser.getId());
        if (!this.isBound(user)) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.mfa.not_bound"));
        List<String> codes = this.applyRecoveryCodes(user);
        eruptDao.merge(user);
        return codes;
    }

    /**
     * How many single use recovery codes the user still holds.
     */
    @Transactional
    public int countRecoveryCodes(Long uid) {
        EruptUser user = eruptDao.getEntityManager().find(EruptUser.class, uid);
        return null == user || null == user.getMfaRecoveryCodes() ? 0 : user.getMfaRecoveryCodes().size();
    }

    /**
     * Drop the binding of the given user and persist it.
     */
    @Transactional
    public void unbind(Long uid) {
        EruptUser user = eruptDao.getEntityManager().find(EruptUser.class, uid);
        if (null == user) return;
        this.unbind(user);
        eruptDao.merge(user);
    }

    /**
     * Drop the binding. Callers are responsible for persisting the user.
     */
    public void unbind(EruptUser eruptUser) {
        eruptUser.setMfaEnabled(false);
        eruptUser.setMfaSecret(null);
        if (null != eruptUser.getMfaRecoveryCodes()) {
            eruptUser.getMfaRecoveryCodes().clear();
        }
        sessionService.remove(SessionKey.MFA_ENROLL + eruptUser.getId());
    }

    private List<String> applyRecoveryCodes(EruptUser user) {
        List<String> codes = new ArrayList<>(RECOVERY_CODE_COUNT);
        for (int i = 0; i < RECOVERY_CODE_COUNT; i++) codes.add(Erupts.generateCode(RECOVERY_CODE_LENGTH).toLowerCase());
        if (null == user.getMfaRecoveryCodes()) user.setMfaRecoveryCodes(new HashSet<>());
        user.getMfaRecoveryCodes().clear();
        codes.forEach(it -> user.getMfaRecoveryCodes().add(hashRecoveryCode(it)));
        return codes;
    }

    private static String hashRecoveryCode(String code) {
        // SHA-256 rather than SHA-512: the codes are 59 bit random tokens, so hash strength
        // is not the constraint, and halving the width keeps the JSON column comfortable
        return EncryptUtil.digestSHA256(code.trim().toLowerCase());
    }

    private String issuer() {
        String issuer = eruptAppProp.getMfa().getIssuer();
        return StringUtils.isBlank(issuer) ? applicationName : issuer;
    }

}
