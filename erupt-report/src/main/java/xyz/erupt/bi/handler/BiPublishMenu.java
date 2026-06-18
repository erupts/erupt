package xyz.erupt.bi.handler;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.bi.config.EruptBiProp;
import xyz.erupt.bi.constant.BiConst;
import xyz.erupt.bi.model.Bi;
import xyz.erupt.bi.model.BiReleaseModal;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptTokenService;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Button logic for publishing a report to menu
 *
 * @author YuePeng
 * date 2023/6/4 17:51
 */
@Component
public class BiPublishMenu implements OperationHandler<Bi, BiReleaseModal> {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptBiProp eruptBiProp;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptContextService eruptContextService;

    @Resource
    private EruptTokenService eruptTokenService;

    @Override
    @Transactional
    public String exec(List<Bi> data, BiReleaseModal biReleaseModal, String[] param) {
        if (eruptBiProp.getSuperAdminPublish() && !eruptUserService.getSimpleUserInfo().isSuperAdmin()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("bi.publish_super_admin_only"));
        }
        Bi bi = data.get(0);
        Erupts.requireNull(eruptDao.lambdaQuery(EruptMenu.class).eq(EruptMenu::getCode, bi.getCode()).one(), I18nTranslate.$translate("bi.menu_already_exists"));
        Integer max = (Integer) eruptDao.lambdaQuery(EruptMenu.class).max(EruptMenu::getSort);
        EruptMenu eruptMenu = new EruptMenu(bi.getCode(), biReleaseModal.getName(), BiConst.MENU_TYPE,
                bi.getCode(), MenuStatus.OPEN.getValue(), max + 10, null, biReleaseModal.getEruptMenu());
        eruptDao.persist(eruptMenu);
        // save to the designated role as well
        {
            String biRoleName = "bi_view_role@auto";
            EruptRole eruptRole = new EruptRole();
            eruptRole.setCode(biRoleName);
            eruptRole.setName("bi-view-role");
            eruptRole.setStatus(true);
            eruptRole.setSort(20);
            eruptRole.setCreateTime(new Date());
            eruptRole.setUpdateTime(new Date());
            EruptRole biRole = eruptDao.persistIfNotExist(EruptRole.class, eruptRole, "code", biRoleName);
            if (null == biRole.getMenus()) {
                biRole.setMenus(new HashSet<>());
            }
            biRole.setUpdateTime(new Date());
            biRole.getMenus().add(eruptMenu);
            eruptDao.persist(biRole);
        }
        // refresh current user menu
        eruptTokenService.loginToken(eruptUserService.getCurrentEruptUser(), eruptContextService.getCurrentToken());
        return null;
    }

    @Override
    public BiReleaseModal eruptFormValue(List<Bi> data, BiReleaseModal biReleaseModal, String[] param) {
        biReleaseModal.setName(data.get(0).getName());
        return OperationHandler.super.eruptFormValue(data, biReleaseModal, param);
    }
}
