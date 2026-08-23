package xyz.erupt.designer.handler;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.designer.model.DesignerEntity;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.enums.EruptFunPermissions;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.input.MenuPublishModal;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptMenuService;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.util.UPMSUtil;

import java.util.List;

/**
 * Publishes a designer form to the navigation menu as a TABLE entry.
 *
 * @author YuePeng
 */
@Component
public class DesignerPublishMenu implements OperationHandler<DesignerEntity, MenuPublishModal> {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptContextService eruptContextService;

    @Resource
    private EruptMenuService eruptMenuService;

    @Override
    @Transactional
    public String exec(List<DesignerEntity> data, MenuPublishModal modal, String[] param) {
        DesignerEntity entity = data.get(0);
        if (null == entity.getConfig() || entity.getConfig().isEmpty()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("designer.publish_first"));
        }
        Erupts.requireNull(
                eruptDao.lambdaQuery(EruptMenu.class).eq(EruptMenu::getCode, entity.getClassName()).one(),
                I18nTranslate.$translate("bi.menu_already_exists")
        );
        Integer max = (Integer) eruptDao.lambdaQuery(EruptMenu.class).max(EruptMenu::getSort);
        EruptMenu menu = new EruptMenu(
                entity.getClassName(), modal.getName(),
                MenuTypeEnum.TABLE.getCode(), entity.getClassName(),
                MenuStatus.OPEN.getValue(), (max == null ? 0 : max) + 10,
                null, modal.getEruptMenu()
        );
        eruptDao.persist(menu);
        int i = 0;
        for (EruptFunPermissions perm : EruptFunPermissions.values()) {
            eruptDao.persist(new EruptMenu(
                    Erupts.generateCode(), perm.getName(), MenuTypeEnum.BUTTON.getCode(),
                    UPMSUtil.getEruptFunPermissionsCode(entity.getClassName(), perm), menu, i += 10
            ));
        }
        eruptMenuService.flushMenuCache();
        return null;
    }

    @Override
    public MenuPublishModal eruptFormValue(List<DesignerEntity> data, MenuPublishModal modal, String[] param) {
        modal.setName(data.get(0).getName());
        return OperationHandler.super.eruptFormValue(data, modal, param);
    }

}
