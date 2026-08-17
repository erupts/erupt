package xyz.erupt.monitor.handler;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.monitor.model.EruptClassInfo;
import xyz.erupt.upms.enums.EruptFunPermissions;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.input.MenuPublishModal;
import xyz.erupt.upms.service.EruptMenuService;
import xyz.erupt.upms.util.UPMSUtil;

import java.util.List;

/**
 * Publishes a registered erupt class to the navigation menu as a TABLE entry
 * with the full set of function-permission button children.
 *
 * @author YuePeng
 */
@Component
public class EruptClassPublishMenu implements OperationHandler<EruptClassInfo, MenuPublishModal> {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptMenuService eruptMenuService;

    @Override
    @Transactional
    public String exec(List<EruptClassInfo> data, MenuPublishModal modal, String[] param) {
        EruptClassInfo info = data.get(0);
        Erupts.requireNull(
                eruptDao.lambdaQuery(EruptMenu.class).eq(EruptMenu::getCode, info.getName()).one(),
                I18nTranslate.$translate("monitor.menu_already_exists")
        );
        Integer max = (Integer) eruptDao.lambdaQuery(EruptMenu.class).max(EruptMenu::getSort);
        EruptMenu menu = new EruptMenu(
                info.getName(), modal.getName(),
                MenuTypeEnum.TABLE.getCode(), info.getName(),
                MenuStatus.OPEN.getValue(), (max == null ? 0 : max) + 10,
                null, modal.getEruptMenu()
        );
        eruptDao.persist(menu);
        int i = 0;
        for (EruptFunPermissions perm : EruptFunPermissions.values()) {
            eruptDao.persist(new EruptMenu(
                    Erupts.generateCode(), perm.getName(), MenuTypeEnum.BUTTON.getCode(),
                    UPMSUtil.getEruptFunPermissionsCode(info.getName(), perm), menu, i += 10
            ));
        }
        eruptMenuService.flushMenuCache();
        return null;
    }

    @Override
    public MenuPublishModal eruptFormValue(List<EruptClassInfo> data, MenuPublishModal modal, String[] param) {
        modal.setName(data.get(0).getDisplayName());
        return OperationHandler.super.eruptFormValue(data, modal, param);
    }

}
