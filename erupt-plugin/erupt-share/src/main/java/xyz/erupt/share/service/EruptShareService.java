package xyz.erupt.share.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.module.MetaMenu;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.upms.enums.EruptFunPermissions;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.util.UPMSUtil;
import xyz.erupt.upms.vo.EruptMenuVo;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.util.EncryptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.share.constant.ShareSessionKey;
import xyz.erupt.share.model.EruptShareLink;
import xyz.erupt.share.prop.EruptShareProp;
import xyz.erupt.share.vo.ShareMetaVo;
import xyz.erupt.share.vo.ShareScope;
import xyz.erupt.share.vo.ShareSessionVo;
import xyz.erupt.upms.helper.UpmsTenant;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptTokenService;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.util.IpUtil;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Opens a share link: checks it, mints a session of the issuer narrowed to the target, and
 * records the visit. Nothing here needs a logged-in caller.
 *
 * @author YuePeng
 */
@Service
public class EruptShareService {

    private static final Pattern CODE = Pattern.compile("^[A-Za-z0-9]{" + EruptShareLink.CODE_LENGTH + "}$");

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptSessionService sessionService;

    @Resource
    private EruptTokenService eruptTokenService;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptShareProp eruptShareProp;

    @Resource
    private HttpServletRequest request;

    @Resource
    private EruptContextService eruptContextService;

    // Menu types a visitor cannot open as a page
    private static final Set<String> NOT_A_PAGE = Set.of(MenuTypeEnum.BUTTON.getCode(), MenuTypeEnum.API.getCode());

    /**
     * Whether a user holding these menu values may open a public form for the model: the model
     * must be local, allow adding, and the user must hold both its menu and its add button.
     */
    public static boolean canAdd(EruptModel model, Map<String, Boolean> menuValues) {
        if (null == model || model.isRemote() || !model.getErupt().power().add()) return false;
        String name = model.getEruptName();
        return menuValues.containsKey(name.toLowerCase())
                && menuValues.containsKey(UPMSUtil.getEruptFunPermissionsCode(name, EruptFunPermissions.ADD).toLowerCase());
    }

    // The models the current user may open a public form for, labelled by the raw @Erupt(name)
    public List<VLModel> formTargets() {
        Map<String, Boolean> menus = eruptUserService.getEruptMenuValuesMap();
        return EruptCoreService.getErupts().stream()
                .filter(it -> canAdd(it, menus))
                .map(it -> new VLModel(it.getEruptName(), it.getErupt().name()))
                .toList();
    }

    // The pages the current user may share: every menu of theirs that opens something, by code
    public List<VLModel> menuTargets() {
        return this.issuerMenus().stream()
                .filter(it -> null != it.getType() && !NOT_A_PAGE.contains(it.getType()) && null != it.getValue())
                .map(it -> new VLModel(it.getCode(), it.getName()))
                .toList();
    }

    private List<EruptMenuVo> issuerMenus() {
        List<EruptMenuVo> menus = eruptTokenService.menuView(eruptContextService.getCurrentToken());
        return null == menus ? List.of() : menus;
    }

    // The code doubles as salt: unique per link, so equal passwords never share a digest
    public static String digest(String code, String password) {
        return StringUtils.isBlank(password) ? null : EncryptUtil.digestSHA512Salt(password, code);
    }

    /**
     * Refuses a link whose target the current user may not share, so a link never grants more
     * than its issuer has. Called when a link is created or changed.
     */
    public void verifyTarget(EruptShareLink link) {
        if (link.isForm()) {
            if (StringUtils.isBlank(link.getTarget())) throw tip("Model not found");
            EruptModel model = EruptCoreService.getErupt(link.getTarget());
            if (null == model) throw tip("Model not found");
            if (model.isRemote()) throw tip("Remote models cannot be shared");
            if (!model.getErupt().power().add()) throw tip("Model does not allow adding records");
            if (!canAdd(model, eruptUserService.getEruptMenuValuesMap())) throw tip("No permission to share this model");
            link.setTarget(model.getEruptName());
        } else if (EruptShareLink.TYPE_MENU.equals(link.getType())) {
            if (this.menuTargets().stream().noneMatch(it -> it.getValue().equals(link.getTarget()))) throw tip("No permission to share this page");
        } else {
            throw tip("Unknown share type");
        }
    }

    public ShareMetaVo meta(String code) {
        EruptShareLink link = find(code);
        if (null == link) {
            return ShareMetaVo.builder().available(false).reason(I18nTranslate.$translate("Share link not found")).build();
        }
        String reason = unavailableReason(link);
        return ShareMetaVo.builder()
                .title(link.getTitle()).type(link.getType()).issuer(link.getIssuerName())
                .requirePassword(null != link.getPasswordHash())
                .available(null == reason).reason(reason)
                .build();
    }

    /**
     * Trade the code (and password) for a session. The session belongs to the issuer, carries only
     * the menus the target needs, lives no longer than the link, and is bound to the link so that
     * revoking the link ends it. Attempts are counted per client address.
     */
    @Transactional
    public ShareSessionVo exchange(String code, String password) {
        // Failed exchanges are counted per client address; a code or password cannot be guessed at speed
        String attemptKey = ShareSessionKey.ATTEMPT + IpUtil.getIpAddr(request);
        Object attempts = sessionService.get(attemptKey);
        if (null != attempts && Long.parseLong(attempts.toString()) >= eruptShareProp.getMaxAttempts()) {
            throw error("Too many attempts, please try again later");
        }
        EruptShareLink link;
        try {
            link = find(code);
            if (null == link) throw error("Share link not found");
            String reason = unavailableReason(link);
            if (null != reason) throw new EruptWebApiRuntimeException(reason);
            if (null != link.getPasswordHash()) {
                if (StringUtils.isBlank(password)) throw error("Password required");
                if (!link.getPasswordHash().equals(digest(link.getCode(), password))) throw error("Wrong password");
            }
        } catch (EruptWebApiRuntimeException e) {
            sessionService.increment(attemptKey, eruptShareProp.getAttemptWindowMinutes(), TimeUnit.MINUTES);
            throw e;
        }
        // Counted in one statement, under the cap: two visitors racing for the last visit cannot both get it
        int counted = eruptDao.getEntityManager().createQuery("update EruptShareLink l set l.visitCount = coalesce(l.visitCount, 0) + 1, "
                        + "l.lastVisitTime = :now where l.id = :id and (l.maxVisits is null or coalesce(l.visitCount, 0) < l.maxVisits)")
                .setParameter("now", new Date()).setParameter("id", link.getId()).executeUpdate();
        if (counted == 0) throw error("Share link visit limit reached");
        EruptMenu menu = link.isForm() ? null : eruptDao.lambdaQuery(EruptMenu.class).eq(EruptMenu::getCode, link.getTarget()).one();
        if (!link.isForm() && null == menu) throw error("Share link not found");
        int minutes = sessionMinutes(link);
        String token = Erupts.generateCode(22);
        // loginToken shortens the expiry by one minute, so it is handed one more
        eruptTokenService.loginToken(issuerInfo(link), targetMenus(link, menu), token, minutes + 1);
        ShareScope scope = new ShareScope(link.getCode(), link.getType(), link.getTarget());
        sessionService.put(ShareSessionKey.SCOPE + token, GsonFactory.getGson().toJson(scope), minutes, TimeUnit.MINUTES);
        this.recordVisit(link, token);
        return link.isForm()
                ? new ShareSessionVo(token, link.getType(), link.getTarget(), null, minutes)
                : new ShareSessionVo(token, link.getType(), menu.getValue(), menu.getType(), minutes);
    }

    /**
     * Ends every session opened from the link. Scopes are scanned by prefix, which both stores
     * support; links are revoked rarely enough for that.
     */
    public void revokeSessions(String code) {
        if (null == code) return;
        for (String key : sessionService.keys(ShareSessionKey.SCOPE)) {
            ShareScope scope = scope(key.substring(ShareSessionKey.SCOPE.length()));
            if (null != scope && code.equals(scope.getCode())) {
                String token = key.substring(ShareSessionKey.SCOPE.length());
                eruptTokenService.logoutToken(code, token);
                sessionService.remove(key);
            }
        }
    }

    // The scope of a session, null for an ordinary login session
    public ShareScope scope(String token) {
        String key = ShareSessionKey.SCOPE + token;
        if (!sessionService.exist(key)) return null;
        return sessionService.get(key, ShareScope.class);
    }

    private EruptShareLink find(String code) {
        if (null == code || !CODE.matcher(code).matches()) return null;
        return eruptDao.lambdaQuery(EruptShareLink.class).eq(EruptShareLink::getCode, code).one();
    }

    private String unavailableReason(EruptShareLink link) {
        if (!Boolean.TRUE.equals(link.getEnabled())) return I18nTranslate.$translate("Share link not found");
        if (null == link.getExpireTime() || link.getExpireTime().before(new Date())) return I18nTranslate.$translate("Share link has expired");
        if (null != link.getMaxVisits() && null != link.getVisitCount() && link.getVisitCount() >= link.getMaxVisits()) {
            return I18nTranslate.$translate("Share link visit limit reached");
        }
        return null;
    }

    // The link's remaining life, capped by the configured session length, at least one minute
    private int sessionMinutes(EruptShareLink link) {
        long remaining = (link.getExpireTime().getTime() - System.currentTimeMillis()) / 60_000;
        return (int) Math.max(1, Math.min(eruptShareProp.getSessionMinutes(), remaining));
    }

    // The issuer as the session knows them; no roles, never an administrator, whatever the issuer is
    private MetaUserinfo issuerInfo(EruptShareLink link) {
        MetaUserinfo info = new MetaUserinfo();
        info.setId(link.getIssuerId());
        info.setAccount(link.getIssuerAccount());
        info.setUsername(link.getIssuerName());
        info.setTenantId(UpmsTenant.isPlatform(link.getTenantId()) ? null : link.getTenantId());
        info.setSuperAdmin(false);
        info.setRoles(Collections.emptyList());
        return info;
    }

    // Exactly the menu values the interceptor and the power handler look for, nothing else: a form
    // gets its model's menu and add button, a page gets the one menu it is, without any button
    private List<MetaMenu> targetMenus(EruptShareLink link, EruptMenu menu) {
        String code = "$share-" + link.getCode();
        if (link.isForm()) {
            return List.of(
                    MetaMenu.createSimpleMenu(code, link.getTitle(), link.getTarget(), null, 0, MenuTypeEnum.TABLE.getCode()),
                    MetaMenu.createSimpleMenu(code + "-add", link.getTitle(),
                            UPMSUtil.getEruptFunPermissionsCode(link.getTarget(), EruptFunPermissions.ADD), null, 1, MenuTypeEnum.BUTTON.getCode())
            );
        }
        return List.of(MetaMenu.createSimpleMenu(code, menu.getName(), menu.getValue(), null, 0, menu.getType()));
    }

    // A visit is a sign-in of the issuer: it goes to the login log, under the issuer's tenant, named by the link
    private void recordVisit(EruptShareLink link, String token) {
        EruptUser issuer = new EruptUser();
        issuer.setName(link.getIssuerName() + " [share " + link.getCode() + "]");
        issuer.setTenantId(link.getTenantId());
        eruptUserService.saveLoginLog(issuer, token);
    }

    private static EruptWebApiRuntimeException error(String message) {
        return new EruptWebApiRuntimeException(I18nTranslate.$translate(message));
    }

    // A refusal shown to the issuer in the admin form, as a dialog rather than a failed request
    private static EruptApiErrorTip tip(String message) {
        return new EruptApiErrorTip(I18nTranslate.$translate(message));
    }
}
