package xyz.erupt.upms.service;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.exception.EruptException;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.constant.MenuTypeEnum;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.enums.EruptFunPermissions;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.spi.UpmsMenuScope;
import xyz.erupt.upms.util.UPMSUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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

    @Resource
    private ObjectProvider<UpmsMenuScope> menuScope;

    /**
     * Menus of a user: every menu for an administrator, the menus of the active roles otherwise,
     * narrowed by each registered {@link UpmsMenuScope} (a tenant's subscription, for instance).
     */
    public List<EruptMenu> getUserAllMenu(EruptUser eruptUser) {
        List<EruptMenu> menus;
        if (null != eruptUser.getIsAdmin() && eruptUser.getIsAdmin()) {
            menus = eruptDao.lambdaQuery(EruptMenu.class).orderBy(EruptMenu::getSort).list();
        } else {
            Set<EruptMenu> menuSet = new HashSet<>();
            eruptUser.getRoles().stream().filter(EruptRole::getStatus).map(EruptRole::getMenus).forEach(menuSet::addAll);
            menus = menuSet.stream().filter(it -> it.getStatus() != MenuStatus.DISABLE.getValue()).collect(Collectors.toList());
        }
        for (UpmsMenuScope scope : menuScope) {
            menus = menus.stream().filter(menu -> scope.allows(eruptUser, menu)).collect(Collectors.toList());
        }
        return this.withFolders(menus);
    }

    /**
     * Add the folders above the given menus that the list lacks. A role or a tenant package grants
     * menus by id, and the sidebar is built down from the roots, so a granted page whose folder was
     * not granted too would vanish: a folder added by a later release (a new root a module moved
     * its pages under) never is. Only pure folders (no type, no value) are added — they open
     * nothing, so this grants no access — and the walk stops at a disabled one, which hides its
     * branch on purpose.
     */
    private List<EruptMenu> withFolders(List<EruptMenu> menus) {
        Set<Long> ids = menus.stream().map(EruptMenu::getId).collect(Collectors.toSet());
        List<EruptMenu> result = new ArrayList<>(menus);
        Map<Long, EruptMenu> all = null;
        for (EruptMenu menu : menus) {
            EruptMenu parent = menu.getParentMenu();
            while (null != parent && !ids.contains(parent.getId())) {
                if (null == all) {
                    all = eruptDao.lambdaQuery(EruptMenu.class).list().stream().collect(Collectors.toMap(EruptMenu::getId, Function.identity()));
                }
                EruptMenu folder = all.get(parent.getId());
                if (null == folder || StringUtils.isNotBlank(folder.getType()) || StringUtils.isNotBlank(folder.getValue())
                        || Objects.equals(folder.getStatus(), MenuStatus.DISABLE.getValue())) {
                    break;
                }
                ids.add(folder.getId());
                result.add(folder);
                parent = folder.getParentMenu();
            }
        }
        return result;
    }

    @Override
    public void validate(EruptMenu eruptMenu) throws EruptException {
        if (StringUtils.isNotBlank(eruptMenu.getType()) && StringUtils.isBlank(eruptMenu.getValue())) {
            throw new EruptException("When selecting a menu type, the type value cannot be empty");
        } else if (StringUtils.isNotBlank(eruptMenu.getValue()) && StringUtils.isBlank(eruptMenu.getType())) {
            throw new EruptException("When has menu value, the menu type cannot be empty");
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
    }


    // Rebuild the current session's menu cache. getCurrentEruptUser() is null for sessions that
    // no longer have a user row, which keep their cached view until they expire.
    public void flushMenuCache() {
        EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
        if (null != eruptUser) eruptTokenService.loginToken(eruptUser, eruptContextService.getCurrentToken());
    }

    @Override
    public void afterAdd(EruptMenu eruptMenu) {
        if (null != eruptMenu.getValue()) {
            EruptFunPermissions[] permissions = EruptFunPermissions.byMenuType(eruptMenu.getType());
            if (null != permissions) {
                int i = 0;
                EruptModel eruptModel = EruptCoreService.getErupt(eruptMenu.getValue());
                for (EruptFunPermissions value : permissions) {
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
        List<EruptMenu> subMenus = eruptDao.lambdaQuery(EruptMenu.class).with(EruptMenu::getParentMenu).eq(EruptMenu::getId, eruptMenu.getId()).with().list();
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
