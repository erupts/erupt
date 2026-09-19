package xyz.erupt.upms.model.input;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptMfaService;

import java.util.List;

/**
 * Clear a user's MFA binding so they can enrol a new authenticator.
 * The secret is never shown to the operator, only destroyed.
 *
 * @author YuePeng
 * date 2026-09-18
 */
@Component
public class ResetMfaExec implements OperationHandler<EruptUser, Void> {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptMfaService eruptMfaService;

    @Override
    @Transactional
    public String exec(List<EruptUser> data, Void unused, String[] param) {
        EruptUser eruptUser = eruptDao.getEntityManager().find(EruptUser.class, data.get(0).getId());
        eruptMfaService.unbind(eruptUser);
        eruptDao.merge(eruptUser);
        return I18nTranslate.$translate("upms.mfa.reset_done");
    }

}
