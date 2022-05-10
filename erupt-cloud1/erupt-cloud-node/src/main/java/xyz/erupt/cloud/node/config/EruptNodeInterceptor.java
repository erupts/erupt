package xyz.erupt.cloud.node.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author YuePeng
 * date 2018-12-20.
 */

@Configuration
@Component
public class EruptNodeInterceptor implements WebMvcConfigurer, AsyncHandlerInterceptor {

    @Resource
    private EruptNodeProp eruptNodeProp;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns(EruptRestPath.ERUPT_API + "/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!eruptNodeProp.getAccessToken().equals(request.getHeader(CloudCommonConst.ACCESS_TOKEN))) {
            throw new EruptWebApiRuntimeException("AccessToken incorrect");
        }
        MetaContext.registerToken(request.getHeader(EruptMutualConst.TOKEN));
        MetaContext.register(new MetaErupt(request.getHeader(EruptMutualConst.ERUPT)));
        //node节点管理的erupt类禁止浏览器直接访问
        response.setHeader("Access-Control-Allow-Origin", "255.255.255.255");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MetaContext.remove();
    }
}
