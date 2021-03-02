package xyz.erupt.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.core.constant.EruptRestPath;

import javax.annotation.Resource;

/**
 * @author liyuepeng
 * @date 2020-08-20
 */
@Configuration
public class WebResourceConfigurer implements WebMvcConfigurer {

    @Resource
    private EruptProp eruptProp;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = eruptProp.getUploadPath();
        if (!eruptProp.getUploadPath().endsWith("/")) {
            uploadPath += "/";
        }
        registry.addResourceHandler(EruptRestPath.ERUPT_ATTACHMENT + "/**").addResourceLocations("file:" + uploadPath);
    }

}