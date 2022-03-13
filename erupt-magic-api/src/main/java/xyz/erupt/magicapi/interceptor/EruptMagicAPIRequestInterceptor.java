package xyz.erupt.magicapi.interceptor;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.core.context.MagicUser;
import org.ssssssss.magicapi.core.interceptor.Authorization;
import org.ssssssss.magicapi.core.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.core.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.core.model.*;
import org.ssssssss.magicapi.datasource.model.DataSourceInfo;
import org.ssssssss.magicapi.function.model.FunctionInfo;
import org.ssssssss.script.MagicScriptContext;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.magicapi.EruptMagicApiAutoConfiguration;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptUserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * magic-api UI鉴权、接口鉴权
 */
@Component
@AllArgsConstructor
public class EruptMagicAPIRequestInterceptor implements RequestInterceptor, AuthorizationInterceptor {

    private final EruptUserService eruptUserService;

    private final EruptContextService eruptContextService;

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
            return new JsonBean<>(401, "用户未登录");
        }
        if (StringUtils.isNotBlank(role) || StringUtils.isNotBlank(permission)) {
            // 未登录
            if (!isLogin) {
                return new JsonBean<Void>(401, "用户未登录");
            } else {
                MetaUserinfo metaUserInfo = eruptUserService.getSimpleUserInfo();
                if (!metaUserInfo.isSuperAdmin()) {
                    // 权限判断
                    if (StringUtils.isNotBlank(permission) && eruptUserService.getEruptMenuByValue(permission) == null) {
                        return new JsonBean<Void>(403, "用户权限不足");
                    }
                    // 角色判断
                    if (StringUtils.isNotBlank(role) && metaUserInfo.getRoles().stream().noneMatch(role::equals)) {
                        return new JsonBean<Void>(403, "用户权限不足");
                    }
                }
            }
        }
        return null;
    }

    /**
     * 配置UI界面不需要登录框
     */
    @Override
    public boolean requireLogin() {
        return false;
    }

    @Override
    public MagicUser getUserByToken(String token) {
        MetaUserinfo metaUserinfo = eruptUserService.getSimpleUserInfoByToken(token);
        if (null == metaUserinfo) {
            throw new RuntimeException("登录已过期");
        }
        return new MagicUser(metaUserinfo.getAccount(), metaUserinfo.getUsername(), token);
    }

    /**
     * 配置UI鉴权
     */
    @Override
    public boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization) {
        if (Authorization.RELOAD == authorization) {
            return true;
        }
        if (eruptUserService.getCurrentUid() == null) {
            throw new EruptWebApiRuntimeException("登录过期！");
        } else if (null == eruptUserService.getEruptMenuByValue(EruptMagicApiAutoConfiguration.MAGIC_API_MENU_PREFIX + authorization.name())) {
            throw new EruptWebApiRuntimeException("权限不足!");
        }
        return true;
    }

    @Override
    public boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization, Group group) {
        if (!eruptUserService.getSimpleUserInfo().isSuperAdmin()) {
            if (group.getOptions().size() > 0) {
                MetaUserinfo metaUserInfo = eruptUserService.getSimpleUserInfo();
                for (BaseDefinition option : group.getOptions()) {
                    if (null != option.getValue() && StringUtils.isNotBlank(option.getValue().toString())) {
                        if (Options.ROLE.getValue().equals(option.getName())) {
                            return metaUserInfo.getRoles().stream().anyMatch(it -> it.equals(option.getValue()));
                        } else if (Options.PERMISSION.getValue().equals(option.getName())) {
                            return null != eruptUserService.getEruptMenuByValue(option.getValue().toString());
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization, MagicEntity entity) {
        if (entity instanceof FunctionInfo) {
            if (Authorization.SAVE == authorization || Authorization.DELETE == authorization) {
                return eruptUserService.getEruptMenuByValue(EruptMagicApiAutoConfiguration.MAGIC_API_MENU_PREFIX + EruptMagicApiAutoConfiguration.FUNCTION) != null;
            }
        } else if (entity instanceof DataSourceInfo) {
            if (Authorization.SAVE == authorization || Authorization.DELETE == authorization) {
                return eruptUserService.getEruptMenuByValue(EruptMagicApiAutoConfiguration.MAGIC_API_MENU_PREFIX + EruptMagicApiAutoConfiguration.DATASOURCE) != null;
            }
        }
        return AuthorizationInterceptor.super.allowVisit(magicUser, request, authorization, entity);
    }

}
