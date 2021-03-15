package xyz.erupt.upms.service;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.upms.config.EruptUpmsConfig;
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

    private final Gson gson = GsonFactory.getGson();
    @Resource
    private EruptSessionService sessionService;

    @Resource
    private HttpServletRequest request;
    @Resource
    private EruptUserService eruptUserService;

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

    @Resource
    private EruptUpmsConfig eruptUpmsConfig;

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
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public List<EruptMenuVo> geneMenuListVo(List<EruptMenu> menus) {
        List<EruptMenuVo> list = new ArrayList<>();
        menus.forEach(menu -> {
            if (Integer.valueOf(EruptMenu.OPEN).equals(menu.getStatus())) {
                Long pid = null;
                if (null != menu.getParentMenu()) {
                    pid = menu.getParentMenu().getId();
                }
                list.add(new EruptMenuVo(menu.getId(), menu.getCode(), menu.getName(), menu.getType(), menu.getValue(), menu.getIcon(), pid));
            }
        });
        return list;
    }

    @Override
    public void afterAdd(EruptMenu eruptMenu) {
        if (eruptMenu.getCode().contains("/")) {
            throw new EruptWebApiRuntimeException("菜单编码禁止出现 '/' 字符");
        }
        if (StringUtils.isNotBlank(eruptMenu.getType()) && StringUtils.isBlank(eruptMenu.getValue())) {
            throw new EruptWebApiRuntimeException("选择菜单类型时，类型值不能为空");
        }
        String token = request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_TOKEN);
        List<EruptMenu> eruptMenus = getMenuList(eruptUserService.getCurrentEruptUser());
        List<EruptMenuVo> menuVoList = geneMenuListVo(eruptMenus);
        sessionService.put(SessionKey.MENU_VIEW + token, gson.toJson(menuVoList), eruptUpmsConfig.getExpireTimeByLogin());
    }

}
