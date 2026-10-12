package xyz.erupt.upms.handler;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.FilterHandler;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.prop.EruptUpmsProp;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.spi.UpmsMenuScope;

import java.util.HashSet;
import java.util.Objects;
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

    @Resource
    private ObjectProvider<UpmsMenuScope> menuScope;

    @Override
    public String filter(String condition, String[] params) {
        EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
        Set<Long> scope = menuScope.stream().map(it -> it.menuIds(eruptUser)).filter(Objects::nonNull)
                .reduce((a, b) -> { Set<Long> both = new HashSet<>(a); both.retainAll(b); return both; }).orElse(null);
        if (eruptUser.getIsAdmin() || !eruptUpmsProp.isStrictRoleMenuLegal()) {
            return null == scope ? null : inCondition(scope);
        }
        Set<EruptMenu> menuSet = new HashSet<>();
        eruptUser.getRoles().stream().filter(EruptRole::getStatus).map(EruptRole::getMenus).forEach(menuSet::addAll);
        Set<Long> ids = menuSet.stream().map(EruptMenu::getId).collect(Collectors.toSet());
        if (null != scope) ids.retainAll(scope);
        return inCondition(ids);
    }

    private static String inCondition(Set<Long> ids) {
        // an empty list is not valid JPQL; no menu has id -1
        if (ids.isEmpty()) return "EruptMenu.id = -1";
        return String.format("EruptMenu.id in (%s)", ids.stream().map(String::valueOf).collect(Collectors.joining(",")));
    }

}
