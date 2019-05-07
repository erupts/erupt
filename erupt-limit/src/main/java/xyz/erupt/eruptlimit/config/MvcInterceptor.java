package xyz.erupt.eruptlimit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.eruptlimit.interceptor.LoginInterceptor;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by liyuepeng on 2018-12-20.
 */

@Configuration
public class MvcInterceptor implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Value("#{'${erupt.allowRequestFileType:html}'.split(',')}")
    private String[] configAllowRequestFileType;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] allowFileType = {"html", "js", "css", "svg", "eot", "woff", "woff2", "ttf", "png", "jpg", "gif"};
        Set<String> types = new HashSet<>();
        for (String s : allowFileType) {
            types.add("/**/**." + s);
        }
        for (String s : configAllowRequestFileType) {
            types.add("/**/**." + s);
        }
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(RestPath.DONT_INTERCEPT + "/**")
                .excludePathPatterns("/error")
                .excludePathPatterns(types.toArray(new String[0]))
                .addPathPatterns("/**");
    }


}
