package xyz.erupt.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.security.interceptor.LoginInterceptor;

import javax.annotation.Resource;

/**
 * @author liyuepeng
 * @date 2018-12-20.
 */

@Configuration
public class MvcInterceptor implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).addPathPatterns(EruptRestPath.ERUPT_API + "/**");
    }

}
