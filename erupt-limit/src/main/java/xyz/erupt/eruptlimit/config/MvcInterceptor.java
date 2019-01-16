package xyz.erupt.eruptlimit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.eruptlimit.interceptor.LoginInterceptor;

import javax.annotation.Resource;

/**
 * Created by liyuepeng on 2018-12-20.
 */

@Configuration
public class MvcInterceptor implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**")
                .excludePathPatterns(RestPath.DONT_INTERCEPT + "/**");
    }


}
