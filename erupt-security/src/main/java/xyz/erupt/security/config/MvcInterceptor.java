package xyz.erupt.security.config;

import jakarta.annotation.Resource;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.security.interceptor.EruptSecurityInterceptor;
import xyz.erupt.security.interceptor.EruptSuperInterceptor;
import xyz.erupt.security.interceptor.HttpServletRequestFilter;

/**
 * @author YuePeng
 * date 2018-12-20.
 */

@Configuration
public class MvcInterceptor implements WebMvcConfigurer {

    @Resource
    private EruptSecurityInterceptor eruptSecurityInterceptor;

    @Resource
    private EruptSuperInterceptor eruptSuperInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(eruptSuperInterceptor).addPathPatterns("/**");
        registry.addInterceptor(eruptSecurityInterceptor).addPathPatterns(EruptRestPath.ERUPT_API + "/**");
    }

    @Bean
    public FilterRegistrationBean<HttpServletRequestFilter> registerAuthFilter(EruptSecurityProp eruptSecurityProp) {
        FilterRegistrationBean<HttpServletRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HttpServletRequestFilter(eruptSecurityProp));
        registration.addUrlPatterns(EruptRestPath.ERUPT_API + "/*");
        registration.setName(HttpServletRequestFilter.class.getSimpleName());
        registration.setOrder(1);
        return registration;
    }


}
