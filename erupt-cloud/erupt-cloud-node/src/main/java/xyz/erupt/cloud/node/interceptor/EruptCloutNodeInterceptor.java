package xyz.erupt.cloud.node.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.service.EruptCoreService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author YuePeng
 * date 2023/7/1 16:17
 */
@Configuration
@Component
public class EruptCloutNodeInterceptor implements WebMvcConfigurer, AsyncHandlerInterceptor {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns(EruptRestPath.ERUPT_API + "/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String erupt = request.getHeader(EruptMutualConst.ERUPT);
        if (null != erupt) {
            if (null == EruptCoreService.getErupt(erupt)) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return false;
            }
        }
        return true;
    }

}
