package xyz.erupt.auth.service;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.auth.config.EruptAuthConfig;
import xyz.erupt.auth.constant.SessionKey;
import xyz.erupt.auth.interceptor.LoginInterceptor;
import xyz.erupt.auth.model.EruptMenu;
import xyz.erupt.auth.model.EruptRole;
import xyz.erupt.auth.model.EruptUser;
import xyz.erupt.core.util.DataHandlerUtil;
import xyz.erupt.core.view.TreeModel;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liyuepeng
 * @date 2020-04-13
 */
@Service
public class MenuService implements DataProxy<EruptMenu> {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SessionService sessionService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private EruptAuthConfig eruptAuthConfig;
    @Autowired
    private Gson gson;
    @Autowired
    private EruptUserService eruptUserService;

    public List<EruptMenu> geneMenuList(EruptUser eruptUser) {
        if (null != eruptUser.getIsAdmin() && eruptUser.getIsAdmin()) {
            return entityManager.createQuery("from EruptMenu order by sort").getResultList();
        } else {
            Set<EruptMenu> menuSet = new HashSet<>();
            for (EruptRole role : eruptUser.getRoles()) {
                if (role.getStatus()) {
                    menuSet.addAll(role.getMenus());
                }
            }
            return menuSet.stream().sorted(Comparator.comparing(EruptMenu::getSort, Comparator.nullsFirst(Integer::compareTo))).collect(Collectors.toList());
        }
    }

    public List<TreeModel> geneMenuTree(List<EruptMenu> menuList) {
        List<TreeModel> treeModels = new ArrayList<>();
        for (EruptMenu eruptMenu : menuList) {
            Long pid = null;
            if (null != eruptMenu.getParentMenu()) {
                pid = eruptMenu.getParentMenu().getId();
            }
            TreeModel treeModel = new TreeModel(eruptMenu.getId(), eruptMenu.getName(), pid, eruptMenu);
            treeModels.add(treeModel);
        }
        return DataHandlerUtil.treeModelToTree(treeModels, null);
    }

    @Override
    public void afterAdd(EruptMenu eruptMenu) {
        String token = request.getHeader(LoginInterceptor.ERUPT_HEADER_TOKEN);
        List<EruptMenu> menuList = geneMenuList(eruptUserService.getCurrentEruptUser());
        List<TreeModel> treeResultModels = geneMenuTree(menuList);
        sessionService.put(SessionKey.MENU_TREE + token, gson.toJson(treeResultModels), eruptAuthConfig.getExpireTimeByLogin());
        sessionService.put(SessionKey.MENU_LIST + token, gson.toJson(menuList), eruptAuthConfig.getExpireTimeByLogin());
    }

    @Override
    public void afterUpdate(EruptMenu eruptMenu) {
        this.afterAdd(eruptMenu);
    }

}
