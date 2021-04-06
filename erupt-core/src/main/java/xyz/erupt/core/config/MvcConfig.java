package xyz.erupt.core.config;


import com.google.gson.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.util.DateUtil;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * @author YuePeng
 * date 10/31/18.
 */
@Configuration
@Component
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private EruptProp eruptProp;

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        Gson gson = new GsonBuilder().setDateFormat(DateUtil.DATE_TIME)
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context)
                        -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern(DateUtil.DATE_TIME))))
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context)
                        -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern(DateUtil.DATE))))
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext)
                        -> LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(DateUtil.DATE_TIME)))
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext)
                        -> LocalDate.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern(DateUtil.DATE)))
                .setLongSerializationPolicy(LongSerializationPolicy.STRING).setExclusionStrategies(new GsonExclusionStrategies())
                .serializeNulls().create();
        converters.add(0, new GsonHttpMessageConverter(gson) {
            @Override
            protected boolean supports(Class<?> clazz) {
                if (null != eruptProp.getJacksonHttpMessageConvertersPackages()) {
                    for (String convertersPackage : eruptProp.getJacksonHttpMessageConvertersPackages()) {
                        if (clazz.getName().startsWith(convertersPackage)) {
                            return false;
                        }
                    }
                }
                return super.supports(clazz);
            }
        });
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = eruptProp.getUploadPath();
        if (!eruptProp.getUploadPath().endsWith("/")) {
            uploadPath += "/";
        }
        registry.addResourceHandler(EruptRestPath.ERUPT_ATTACHMENT + "/**").addResourceLocations("file:" + uploadPath);
    }

}
