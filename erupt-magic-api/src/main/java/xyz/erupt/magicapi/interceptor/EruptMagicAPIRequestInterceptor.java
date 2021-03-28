package xyz.erupt.magicapi.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.interceptor.MagicUser;
import org.ssssssss.magicapi.interceptor.RequestInterceptor;
import org.ssssssss.magicapi.model.ApiInfo;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.model.Options;
import org.ssssssss.script.MagicScriptContext;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * magic-api UI鉴权、接口鉴权
 */
@Component
public class EruptMagicAPIRequestInterceptor implements RequestInterceptor, AuthorizationInterceptor {


	private final EruptUserService eruptUserService;

	public EruptMagicAPIRequestInterceptor(EruptUserService eruptUserService) {
		this.eruptUserService = eruptUserService;
	}

	/**
	 * 配置UI界面不需要登录框
	 */
	@Override
	public boolean requireLogin() {
		return false;
	}

	/**
	 * 配置接口权限
	 */
	@Override
	public Object preHandle(ApiInfo info, MagicScriptContext context, HttpServletRequest request, HttpServletResponse response) {
		String permission = Objects.toString(info.getOptionValue(Options.PERMISSION),"");
		String role = Objects.toString(info.getOptionValue(Options.ROLE),"");
		if(StringUtils.isNotBlank(permission) || StringUtils.isNotBlank(role)){
			EruptUser user = eruptUserService.getCurrentEruptUser();
			// 未登录
			if(user == null){
				return new JsonBean<Void>(401,"用户未登录");
			}else {
				// 权限判断
				if(StringUtils.isNotBlank(permission) && eruptUserService.getEruptMenuByValue(permission) == null){
					return new JsonBean<Void>(403,"用户权限不足");
				}
				// 角色判断
				if(StringUtils.isNotBlank(role) && user.getRoles().stream().anyMatch(it -> role.equals(it.getCode()))){
					return new JsonBean<Void>(403,"用户权限不足");
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
		return eruptUserService.getCurrentEruptUser() != null && eruptUserService.getEruptMenuByValue(authorization.name()) != null;
	}
}
