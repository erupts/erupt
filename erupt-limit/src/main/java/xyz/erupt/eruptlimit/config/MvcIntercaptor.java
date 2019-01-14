package xyz.erupt.eruptlimit.config;

import xyz.erupt.core.constant.RestPath;
import xyz.erupt.eruptlimit.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Created by liyuepeng on 2018-12-20.
 */
@Configuration
public class MvcIntercaptor extends WebMvcConfigurationSupport {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns(RestPath.ERUPT_API + "/**");
        super.addInterceptors(registry);
    }
}
