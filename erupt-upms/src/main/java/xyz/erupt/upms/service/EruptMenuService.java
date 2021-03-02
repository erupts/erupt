package xyz.erupt.upms.service;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.vo.EruptMenuVo;

import javax.annotation.Resource;
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
public class EruptMenuService implements DataProxy<EruptMenu> {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EruptSessionService sessionService;

    @Autowired
    private EruptUserService eruptUserService;

    private final Gson gson = GsonFactory.getGson();
    @Resource
    private HttpServletRequest request;

    public List<EruptMenu> getMenuList(EruptUser eruptUser) {
        List<EruptMenu> menus;
        if (null != eruptUser.getIsAdmin() && eruptUser.getIsAdmin()) {
            menus = entityManager.createQuery("from EruptMenu order by sort", EruptMenu.class).getResultList();
        } else {
            Set<EruptMenu> menuSet = new HashSet<>();
            for (EruptRole role : eruptUser.getRoles()) {
                if (role.getStatus()) {
                    menuSet.addAll(role.getMenus());
                }
            }
            menus = menuSet.stream().sorted(Comparator.comparing(EruptMenu::getSort, Comparator.nullsFirst(Integer::compareTo))).collect(Collectors.toList());
        }
        return menus;
    }


    public List<EruptMenuVo> geneMenuListVo(List<EruptMenu> menus) {
        List<EruptMenuVo> list = new ArrayList<>();
        menus.forEach(menu -> {
            if (Integer.valueOf(EruptMenu.OPEN).equals(menu.getStatus())) {
                Long pid = null;
                if (null != menu.getParentMenu()) {
                    pid = menu.getParentMenu().getId();
                }
                list.add(new EruptMenuVo(menu.getId(), menu.getName(), menu.getType(), menu.getValue(), menu.getIcon(), pid));
            }
        });
        return list;
    }

    @Override
    public void afterAdd(EruptMenu eruptMenu) {
        String token = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN);
        List<EruptMenu> eruptMenus = getMenuList(eruptUserService.getCurrentEruptUser());
        List<EruptMenuVo> menuVoList = geneMenuListVo(eruptMenus);
        sessionService.putByLoginExpire(SessionKey.MENU + token, gson.toJson(eruptMenus));
        sessionService.putByLoginExpire(SessionKey.MENU_VIEW + token, gson.toJson(menuVoList));
    }

    @Override
    public void afterUpdate(EruptMenu eruptMenu) {
        this.afterAdd(eruptMenu);
    }


    @Override
    public void afterDelete(EruptMenu eruptMenu) {
        this.afterAdd(eruptMenu);
    }

    @Override
    public void addBehavior(EruptMenu eruptMenu) {
        Integer obj = (Integer) entityManager
                .createQuery("select max(sort) from " + EruptMenu.class.getSimpleName())
                .getSingleResult();
        if (null != obj) {
            eruptMenu.setSort(obj + 10);
        }
        eruptMenu.setStatus(Integer.valueOf(EruptMenu.OPEN));
    }
}
