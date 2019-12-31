package xyz.erupt.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.auth.interceptor.LoginInterceptor;
import xyz.erupt.core.config.EruptConfig;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * @author liyuepeng
 * @date 2018-12-20.
 */

@Configuration
public class MvcInterceptor implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Autowired
    private EruptConfig eruptConfig;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] allowFileType = {"html", "js", "css", "svg", "eot", "woff", "woff2", "ttf", "png", "jpg", "gif", "pdf"};
        Set<String> types = new HashSet<>();
        for (String s : allowFileType) {
            types.add("/**/**." + s);
        }
        for (String s : eruptConfig.getAllowRequestFileType()) {
            types.add("/**/**." + s);
        }
        for (String s : eruptConfig.getAllowRequestFileType()) {
            types.add("/#/**");
        }
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns("/error")
                .excludePathPatterns(types.toArray(new String[0]))
                .addPathPatterns("/**");
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/erupt/**").addResourceLocations("classpath:/erupt-web/");
//    }
}
