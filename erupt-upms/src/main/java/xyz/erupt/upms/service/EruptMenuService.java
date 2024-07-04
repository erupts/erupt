package xyz.erupt.upms.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.enums.EruptFunPermissions;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.util.UPMSUtil;
import xyz.erupt.upms.vo.EruptMenuVo;

import javax.annotation.Resource;
import java.util.*;
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

    public List<EruptMenuVo> geneMenuListVo(List<EruptMenu> menus) {
        List<EruptMenuVo> list = new ArrayList<>();
        menus.stream().filter(menu -> menu.getStatus() == MenuStatus.OPEN.getValue()).forEach(menu -> {
            Long pid = null == menu.getParentMenu() ? null : menu.getParentMenu().getId();
            list.add(new EruptMenuVo(menu.getId(), menu.getCode(), menu.getName(), menu.getType(), menu.getValue(), menu.getIcon(), pid));
        });
        return list;
    }

    public List<EruptMenu> getUserAllMenu(EruptUser eruptUser) {
        if (null != eruptUser.getIsAdmin() && eruptUser.getIsAdmin()) {
            return eruptDao.lambdaQuery(EruptMenu.class).orderBy(EruptMenu::getSort).list();
        } else {
            Set<EruptMenu> menuSet = new HashSet<>();
            eruptUser.getRoles().stream().filter(EruptRole::getStatus).map(EruptRole::getMenus).forEach(menuSet::addAll);
            return menuSet.stream().filter(it -> it.getStatus() != MenuStatus.DISABLE.getValue()).sorted(Comparator.comparing(EruptMenu::getSort, Comparator.nullsFirst(Integer::compareTo))).collect(Collectors.toList());
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


    private void flushCache() {
        EruptUserService eruptUserService = EruptSpringUtil.getBean(EruptUserService.class);
        eruptUserService.cacheUserInfo(eruptUserService.getCurrentEruptUser(), eruptContextService.getCurrentToken());
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
        this.flushCache();
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
    }

    @Override
    public void beforeDelete(EruptMenu eruptMenu) {
        eruptDao.getJdbcTemplate().update("delete from e_upms_role_menu where menu_id = " + eruptMenu.getId());
    }

    @Override
    public void afterDelete(EruptMenu eruptMenu) {
        this.flushCache();
    }

}
