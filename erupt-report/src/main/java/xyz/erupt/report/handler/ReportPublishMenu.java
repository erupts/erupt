package xyz.erupt.report.handler;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.report.config.EruptReportProp;
import xyz.erupt.report.constant.ReportConst;
import xyz.erupt.report.model.Bi;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.input.MenuPublishModal;
import xyz.erupt.upms.service.EruptMenuService;
import xyz.erupt.upms.service.EruptUserService;

import java.util.List;

/**
 * Button logic for publishing a report to menu
 *
 * @author YuePeng
 * date 2023/6/4 17:51
 */
@Component
public class ReportPublishMenu implements OperationHandler<Bi, MenuPublishModal> {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptReportProp eruptBiProp;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptMenuService eruptMenuService;

    @Override
    @Transactional
    public String exec(List<Bi> data, MenuPublishModal biReleaseModal, String[] param) {
        if (eruptBiProp.getSuperAdminPublish() && !eruptUserService.getSimpleUserInfo().isSuperAdmin()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("bi.publish_super_admin_only"));
        }
        Bi bi = data.get(0);
        Erupts.requireNull(eruptDao.lambdaQuery(EruptMenu.class).eq(EruptMenu::getCode, bi.getCode()).one(), I18nTranslate.$translate("bi.menu_already_exists"));
        Integer max = (Integer) eruptDao.lambdaQuery(EruptMenu.class).max(EruptMenu::getSort);
        EruptMenu eruptMenu = new EruptMenu(bi.getCode(), biReleaseModal.getName(), ReportConst.MENU_TYPE,
                bi.getCode(), MenuStatus.OPEN.getValue(), max + 10, null, biReleaseModal.getEruptMenu());
        eruptDao.persist(eruptMenu);
        eruptMenuService.flushMenuCache();
        return null;
    }

    @Override
    public MenuPublishModal eruptFormValue(List<Bi> data, MenuPublishModal biReleaseModal, String[] param) {
        biReleaseModal.setName(data.get(0).getName());
        return OperationHandler.super.eruptFormValue(data, biReleaseModal, param);
    }
}
