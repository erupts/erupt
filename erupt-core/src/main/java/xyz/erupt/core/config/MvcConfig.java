package xyz.erupt.core.config;


import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.prop.EruptProp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author YuePeng
 * date 10/31/18.
 */
@RequiredArgsConstructor
@Configuration
@Component
public class MvcConfig implements WebMvcConfigurer {

    private final EruptProp eruptProp;

    private final Set<String> gsonMessageConverterPackage = Stream.of(EruptConst.BASE_PACKAGE, Gson.class.getPackage().getName()).collect(Collectors.toSet());

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        Optional.ofNullable(eruptProp.getGsonHttpMessageConvertersPackages()).ifPresent(it -> gsonMessageConverterPackage.addAll(Arrays.asList(it)));
        converters.add(0, new GsonHttpMessageConverter(GsonFactory.getGson()) {
            @Override
            protected boolean supports(Class<?> clazz) {
                for (String pack : gsonMessageConverterPackage) {
                    if (clazz.getName().startsWith(pack)) {
                        return super.supports(clazz);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = eruptProp.getUploadPath().endsWith("/") ? eruptProp.getUploadPath() : eruptProp.getUploadPath() + "/";
        ResourceHandlerRegistration resourceHandlerRegistration = registry.addResourceHandler(EruptRestPath.ERUPT_ATTACHMENT + "/**");
        if (uploadPath.startsWith("classpath:")) {
            resourceHandlerRegistration.addResourceLocations(uploadPath);
        } else {
            resourceHandlerRegistration.addResourceLocations("file:" + uploadPath);
        }
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper helper = new UrlPathHelper();
        helper.setUrlDecode(false);
        helper.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configurer.setUrlPathHelper(helper);
    }

}
