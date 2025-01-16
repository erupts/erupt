package xyz.erupt.upms.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.enums.EruptFunPermissions;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.util.UPMSUtil;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2020-04-13
 */
@Service
public class EruptMenuService implements DataProxy<EruptMenu> {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptContextService eruptContextService;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptTokenService eruptTokenService;

    public List<EruptMenu> getUserAllMenu(EruptUser eruptUser) {
        if (null != eruptUser.getIsAdmin() && eruptUser.getIsAdmin()) {
            return eruptDao.lambdaQuery(EruptMenu.class).orderBy(EruptMenu::getSort).list();
        } else {
            Set<EruptMenu> menuSet = new HashSet<>();
            eruptUser.getRoles().stream().filter(EruptRole::getStatus).map(EruptRole::getMenus).forEach(menuSet::addAll);
            return menuSet.stream().filter(it -> it.getStatus() != MenuStatus.DISABLE.getValue()).collect(Collectors.toList());
        }
    }

    @Override
    public void addBehavior(EruptMenu eruptMenu) {
        Integer sort = (Integer) eruptDao.lambdaQuery(EruptMenu.class).max(EruptMenu::getSort);
        Optional.ofNullable(sort).ifPresent(it -> eruptMenu.setSort(it + 10));
        eruptMenu.setStatus(MenuStatus.OPEN.getValue());
    }

    @Override
    public void beforeAdd(EruptMenu eruptMenu) {
        if (null == eruptMenu.getCode()) eruptMenu.setCode(Erupts.generateCode());
        if (StringUtils.isNotBlank(eruptMenu.getType()) && StringUtils.isBlank(eruptMenu.getValue())) {
            throw new EruptWebApiRuntimeException("When selecting a menu type, the type value cannot be empty");
        } else if (StringUtils.isNotBlank(eruptMenu.getValue()) && StringUtils.isBlank(eruptMenu.getType())) {
            throw new EruptWebApiRuntimeException("When has menu value, the menu type cannot be empty");
        }
    }

    @Override
    public void beforeUpdate(EruptMenu eruptMenu) {
        this.beforeAdd(eruptMenu);
    }


    public void flushMenuCache() {
        eruptTokenService.loginToken(eruptUserService.getCurrentEruptUser(), eruptContextService.getCurrentToken());
    }

    @Override
    public void afterAdd(EruptMenu eruptMenu) {
        if (null != eruptMenu.getValue()) {
            if (MenuTypeEnum.TABLE.getCode().equals(eruptMenu.getType()) || MenuTypeEnum.TREE.getCode().equals(eruptMenu.getType())) {
                int i = 0;
                EruptModel eruptModel = EruptCoreService.getErupt(eruptMenu.getValue());
                for (EruptFunPermissions value : EruptFunPermissions.values()) {
                    if (eruptModel == null || value.verifyPower(eruptModel.getErupt().power())) {
                        eruptDao.persist(new EruptMenu(
                                Erupts.generateCode(), value.getName(), MenuTypeEnum.BUTTON.getCode(),
                                UPMSUtil.getEruptFunPermissionsCode(eruptMenu.getValue(), value), eruptMenu, i += 10)
                        );
                    }
                }
            }
        }
        this.flushMenuCache();
    }

    @Override
    public void afterUpdate(EruptMenu eruptMenu) {
        List<EruptMenu> subMenus = eruptDao.lambdaQuery(EruptMenu.class).addCondition("parentMenu.id = " + eruptMenu.getId()).list();
        for (EruptMenu subMenu : subMenus) {
            if (null != subMenu.getValue() && subMenu.getValue().contains("@")) {
                String[] arr = subMenu.getValue().split("@");
                try {
                    EruptFunPermissions.valueOf(arr[1]);
                    if (!arr[0].equals(eruptMenu.getValue())) {
                        subMenu.setValue(eruptMenu.getValue() + "@" + arr[1]);
                        eruptDao.merge(subMenu);
                    }
                } catch (Exception ignore) {
                }
            }
        }
        this.flushMenuCache();
    }

    @Override
    public void beforeDelete(EruptMenu eruptMenu) {
        eruptDao.getJdbcTemplate().update("delete from e_upms_role_menu where menu_id = " + eruptMenu.getId());
    }

    @Override
    public void afterDelete(EruptMenu eruptMenu) {
        this.flushMenuCache();
    }

}
