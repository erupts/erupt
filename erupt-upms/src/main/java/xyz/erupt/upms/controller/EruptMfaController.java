package xyz.erupt.upms.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.SecretUtil;
import xyz.erupt.core.view.R;
import xyz.erupt.upms.base.MfaBody;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptAppProp;
import xyz.erupt.upms.service.EruptMfaService;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.vo.EruptMfaEnrollVo;
import xyz.erupt.upms.vo.EruptMfaStatusVo;

import java.util.List;

/**
 * Self service endpoints for binding and removing a TOTP authenticator.
 * Every route here requires an authenticated session; the login side of MFA
 * lives in {@link EruptUserController}.
 *
 * @author YuePeng
 * date 2026-09-18
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/mfa")
public class EruptMfaController {

    @Resource
    private EruptMfaService eruptMfaService;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptAppProp eruptAppProp;

    @GetMapping("/status")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptMfaStatusVo status() {
        EruptUser eruptUser = this.currentUser();
        EruptMfaStatusVo vo = new EruptMfaStatusVo();
        vo.setEnable(eruptMfaService.isEnable());
        vo.setBound(eruptMfaService.isBound(eruptUser));
        if (vo.isBound()) {
            vo.setRecoveryCodeCount(eruptMfaService.countRecoveryCodes(eruptUser.getId()));
        }
        return vo;
    }

    @PostMapping("/enroll")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<EruptMfaEnrollVo> enroll() {
        EruptUser eruptUser = this.currentUser();
        this.assertEnable();
        if (eruptMfaService.isBound(eruptUser)) {
            return R.error(I18nTranslate.$translate("upms.mfa.already_bound"));
        }
        return R.ok(eruptMfaService.startEnroll(eruptUser));
    }

    @PostMapping("/enroll-confirm")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<List<String>> enrollConfirm(@RequestBody MfaBody body) {
        this.assertEnable();
        return R.ok(eruptMfaService.confirmEnroll(this.currentUser(), body.getCode()));
    }

    @PostMapping("/recovery-codes")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<List<String>> recoveryCodes(@RequestBody MfaBody body) {
        this.assertEnable();
        EruptUser eruptUser = this.currentUser();
        if (!eruptMfaService.verify(eruptUser, body.getCode())) {
            return R.error(I18nTranslate.$translate("upms.mfa.code_error"));
        }
        return R.ok(eruptMfaService.regenerateRecoveryCodes(eruptUser));
    }

    /**
     * Removing a factor is as sensitive as adding one, so it costs both the password
     * and a live code.
     */
    @PostMapping("/unbind")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<Void> unbind(@RequestBody MfaBody body) {
        EruptUser eruptUser = this.currentUser();
        if (!eruptMfaService.isBound(eruptUser)) {
            return R.error(I18nTranslate.$translate("upms.mfa.not_bound"));
        }
        String pwd = eruptAppProp.getPwdTransferEncrypt() ? SecretUtil.decodeSecret(body.getPwd(), 3) : body.getPwd();
        if (!eruptUserService.checkPwd(eruptUser, pwd)) {
            return R.error(I18nTranslate.$translate("upms.pwd_error"));
        }
        if (!eruptMfaService.verify(eruptUser, body.getCode())) {
            return R.error(I18nTranslate.$translate("upms.mfa.code_error"));
        }
        eruptMfaService.unbind(eruptUser.getId());
        return R.ok();
    }

    private void assertEnable() {
        if (!eruptMfaService.isEnable()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.mfa.disabled"));
        }
    }

    private EruptUser currentUser() {
        EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
        if (null == eruptUser) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("upms.mfa.not_bound"));
        return eruptUser;
    }

}
