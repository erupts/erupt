package xyz.erupt.upms.handler;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.FilterHandler;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2021/7/25 12:25
 */
@Service
public class RoleMenuFilter implements FilterHandler {

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptUpmsProp eruptUpmsProp;

    @Override
    public String filter(String condition, String[] params) {
        EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
        if (eruptUser.getIsAdmin() || !eruptUpmsProp.isStrictRoleMenuLegal()) {
            return null;
        } else {
            Set<EruptMenu> menuSet = new HashSet<>();
            eruptUser.getRoles().stream().filter(EruptRole::getStatus).map(EruptRole::getMenus).forEach(menuSet::addAll);
            List<String> menus = menuSet.stream().map(it -> it.getId().toString()).collect(Collectors.toList());
            return String.format("EruptMenu.id in (%s)", String.join(",", String.join(",", menus)));
        }
    }

}
