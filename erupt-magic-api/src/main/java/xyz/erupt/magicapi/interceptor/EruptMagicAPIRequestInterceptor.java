package xyz.erupt.magicapi.interceptor;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.interceptor.MagicUser;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.model.*;
import org.ssssssss.script.MagicScriptContext;
import xyz.erupt.magicapi.EruptMagicApiAutoConfiguration;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.vo.AdminUserinfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;

/**
 * magic-api UI鉴权、接口鉴权
 */
@Component
@AllArgsConstructor
public class EruptMagicAPIRequestInterceptor implements RequestInterceptor, AuthorizationInterceptor {

    private final EruptUserService eruptUserService;

    private final EruptContextService eruptContextService;

    private final HttpServletRequest request;

    /**
     * 配置UI界面不需要登录框
     */
    @Override
    public boolean requireLogin() {
        try {
            Optional.ofNullable(eruptUserService.getSimpleUserInfo()).ifPresent(adminUserInfo -> request.setAttribute(Constants.ATTRIBUTE_MAGIC_USER,
                    new MagicUser(adminUserInfo.getAccount(), adminUserInfo.getUsername(), this.eruptContextService.getCurrentToken())));
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * 配置接口权限
     */
    @Override
    public Object preHandle(ApiInfo info, MagicScriptContext context, HttpServletRequest request, HttpServletResponse response) {
        String permission = Objects.toString(info.getOptionValue(Options.PERMISSION), "");
        String role = Objects.toString(info.getOptionValue(Options.ROLE), "");
        String login = Objects.toString(info.getOptionValue(Options.REQUIRE_LOGIN), "");
        boolean isLogin = eruptUserService.getCurrentUid() != null;
        if (StringUtils.isNotBlank(login) && !isLogin) {
            return new JsonBean<Void>(401, "用户未登录");
        }
        if (StringUtils.isNotBlank(role) || StringUtils.isNotBlank(permission)) {
            // 未登录
            if (!isLogin) {
                return new JsonBean<Void>(401, "用户未登录");
            } else {
                AdminUserinfo adminUserInfo = eruptUserService.getSimpleUserInfo();
                // 权限判断
                if (StringUtils.isNotBlank(permission) && eruptUserService.getEruptMenuByValue(permission) == null) {
                    return new JsonBean<Void>(403, "用户权限不足");
                }
                // 角色判断
                if (StringUtils.isNotBlank(role) && adminUserInfo.getRoles().stream().noneMatch(role::equals)) {
                    return new JsonBean<Void>(403, "用户权限不足");
                }
            }
        }
        return null;
    }

    /**
     * 配置UI鉴权
     */
    @Override
    public boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization) {
        // 未登录或UI权限不足
        return eruptUserService.getCurrentUid() != null
                && eruptUserService.getEruptMenuByValue(EruptMagicApiAutoConfiguration.MAGIC_API_MENU_PREFIX + authorization.name()) != null;
    }

    @Override
    public boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization, DataSourceInfo dataSourceInfo) {
        if (Authorization.SAVE == authorization || Authorization.DELETE == authorization) {
            return eruptUserService.getEruptMenuByValue(EruptMagicApiAutoConfiguration.MAGIC_API_MENU_PREFIX + EruptMagicApiAutoConfiguration.DATASOURCE) != null;
        }
        return true;
    }

    @Override
    public boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization, FunctionInfo functionInfo) {
        if (Authorization.SAVE == authorization || Authorization.DELETE == authorization) {
            return eruptUserService.getEruptMenuByValue(EruptMagicApiAutoConfiguration.MAGIC_API_MENU_PREFIX + EruptMagicApiAutoConfiguration.FUNCTION) != null;
        }
        return true;
    }
}
