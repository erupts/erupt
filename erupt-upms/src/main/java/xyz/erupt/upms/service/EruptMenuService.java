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
            return eruptDao.queryEntityList(EruptMenu.class, "1 = 1 order by sort");
        } else {
            Set<EruptMenu> menuSet = new HashSet<>();
            eruptUser.getRoles().stream().filter(EruptRole::getStatus).map(EruptRole::getMenus).forEach(menuSet::addAll);
            return menuSet.stream().filter(it -> it.getStatus() != MenuStatus.DISABLE.getValue()).sorted(Comparator.comparing(EruptMenu::getSort, Comparator.nullsFirst(Integer::compareTo))).collect(Collectors.toList());
        }
    }

    @Override
    public void addBehavior(EruptMenu eruptMenu) {
        Integer obj = (Integer) eruptDao.getEntityManager().createQuery("select max(sort) from " + EruptMenu.class.getSimpleName()).getSingleResult();
        Optional.ofNullable(obj).ifPresent(it -> eruptMenu.setSort(it + 10));
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

    /**
     * The reason for that:
     * <p>
     * The dependencies of some of the beans in the application context form a cycle:
     * mvcInterceptor
     * ↓
     * eruptSecurityInterceptor
     * ┌─────┐
     * |  eruptUserService
     * ↑     ↓
     * |  eruptMenuService
     * └─────┘
     */
    private void flushCache() {
        EruptUserService eruptUserService = EruptSpringUtil.getBean(EruptUserService.class);
        eruptUserService.cacheUserInfo(eruptUserService.getCurrentEruptUser(), eruptContextService.getCurrentToken());
    }


    @Override
    public void afterAdd(EruptMenu eruptMenu) {
        if (null != eruptMenu.getValue()) {
            if (MenuTypeEnum.TABLE.getCode().equals(eruptMenu.getType()) || MenuTypeEnum.TREE.getCode().equals(eruptMenu.getType())) {
                int i = 0;
                Integer counter = eruptDao.getJdbcTemplate().queryForObject(
                        String.format("select count(*) from e_upms_menu where parent_menu_id = %d", eruptMenu.getId()), Integer.class
                );
                if (null != counter) {
                    if (counter > 0) {
                        // 查询有权限菜单
                        Integer realCounter = eruptDao.getJdbcTemplate().queryForObject(
                                String.format("select count(*) from e_upms_menu where parent_menu_id = %d and value like '%s@%%'", eruptMenu.getId(), eruptMenu.getValue()), Integer.class
                        );
                        // 如果没有查询出权限菜单，那么本次修改Value
                        if (null != realCounter && realCounter == 0) {
                            eruptDao.getJdbcTemplate().update(String.format("delete from e_upms_menu where parent_menu_id = %d and value like '%%@%%'", eruptMenu.getId()));
                            counter = 0;
                        }
                    }
                    if (counter <= 0) {
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
            }
        }
        this.flushCache();
    }

    @Override
    public void afterUpdate(EruptMenu eruptMenu) {
        this.afterAdd(eruptMenu);
    }

    @Override
    public void afterDelete(EruptMenu eruptMenu) {
        this.flushCache();
    }

}
