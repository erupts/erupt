package xyz.erupt.magicapi.interceptor;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.core.context.MagicUser;
import org.ssssssss.magicapi.core.context.RequestEntity;
import org.ssssssss.magicapi.core.exception.MagicLoginException;
import org.ssssssss.magicapi.core.interceptor.Authorization;
import org.ssssssss.magicapi.core.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.core.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.core.model.*;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;
import org.ssssssss.magicapi.datasource.model.DataSourceInfo;
import org.ssssssss.magicapi.function.model.FunctionInfo;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.magicapi.EruptMagicApiAutoConfiguration;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Objects;

/**
 * magic-api UI authentication, interface authentication
 */
@Component
@AllArgsConstructor
public class EruptMagicAPIRequestInterceptor implements RequestInterceptor, AuthorizationInterceptor {

    private final EruptUserService eruptUserService;

    private static final String NO_PERMISSION = "No permission!";

    private static final String LOGIN_EXPIRE = "Login credentials expired!";

    /**
     * Configure interface permissions
     */
    @Override
    public Object preHandle(RequestEntity requestEntity) {
        ApiInfo info = requestEntity.getApiInfo();
        String permission = Objects.toString(info.getOptionValue(Options.PERMISSION), "");
        String role = Objects.toString(info.getOptionValue(Options.ROLE), "");
        String login = Objects.toString(info.getOptionValue(Options.REQUIRE_LOGIN), "");
        boolean isLogin = eruptUserService.getCurrentUid() != null;
        if (StringUtils.isNotBlank(login) && !isLogin) return new JsonBean<>(401, I18nTranslate.$translate(LOGIN_EXPIRE));
        if (StringUtils.isNotBlank(role) || StringUtils.isNotBlank(permission)) {
            if (!isLogin) {
                return new JsonBean<Void>(401, I18nTranslate.$translate(LOGIN_EXPIRE));
            } else {
                MetaUserinfo metaUserInfo = eruptUserService.getSimpleUserInfo();
                if (!metaUserInfo.isSuperAdmin()) {
                    // Menu judgment
                    if (StringUtils.isNotBlank(permission) && eruptUserService.getEruptMenuByValue(permission) == null) {
                        return new JsonBean<Void>(403, I18nTranslate.$translate(NO_PERMISSION));
                    }
                    // Role judgment
                    if (StringUtils.isNotBlank(role) && metaUserInfo.getRoles().stream().noneMatch(role::equals)) {
                        return new JsonBean<Void>(403, I18nTranslate.$translate(NO_PERMISSION));
                    }
                }
            }
        }
        return null;
    }

    /**
     * The UI interface configuration does not require a login box.
     */
    @Override
    public boolean requireLogin() {
        return false;
    }

    @Override
    @SneakyThrows
    public MagicUser getUserByToken(String token) {
        MetaUserinfo metaUserinfo = eruptUserService.getSimpleUserInfoByToken(token);
        if (null == metaUserinfo) throw new MagicLoginException(I18nTranslate.$translate(LOGIN_EXPIRE));
        return new MagicUser(metaUserinfo.getAccount(), metaUserinfo.getUsername(), token);
    }

    /**
     * Configure UI authentication
     */
    @Override
    public boolean allowVisit(MagicUser magicUser, MagicHttpServletRequest request, Authorization authorization) {
        if (eruptUserService.getCurrentUid() == null) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate(LOGIN_EXPIRE));
        } else if (null == eruptUserService.getEruptMenuByValue(EruptMagicApiAutoConfiguration.MAGIC_API_MENU_PREFIX + authorization.name())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate(NO_PERMISSION));
        }
        return true;
    }

    @Override
    public boolean allowVisit(MagicUser magicUser, MagicHttpServletRequest request, Authorization authorization, Group group) {
        if (null == eruptUserService.getCurrentUid()) throw new EruptWebApiRuntimeException(I18nTranslate.$translate(LOGIN_EXPIRE));
        MetaUserinfo metaUserinfo = eruptUserService.getSimpleUserInfo();
        if (!metaUserinfo.isSuperAdmin()) {
            if (!group.getOptions().isEmpty()) {
                for (BaseDefinition option : group.getOptions()) {
                    if (null != option.getValue() && StringUtils.isNotBlank(option.getValue().toString())) {
                        if (Options.ROLE.getValue().equals(option.getName())) {
                            return metaUserinfo.getRoles().stream().anyMatch(it -> it.equals(option.getValue()));
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
    public boolean allowVisit(MagicUser magicUser, MagicHttpServletRequest request, Authorization authorization, MagicEntity entity) {
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
