package xyz.erupt.core.config;


import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.util.DateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
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
        if (null != eruptProp.getGsonHttpMessageConvertersPackages()) {
            gsonMessageConverterPackage.addAll(Arrays.asList(eruptProp.getGsonHttpMessageConvertersPackages()));
        }
        converters.add(0, new GsonHttpMessageConverter(gson) {
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
        String uploadPath = eruptProp.getUploadPath();
        if (!eruptProp.getUploadPath().endsWith("/")) {
            uploadPath += "/";
        }
        registry.addResourceHandler(EruptRestPath.ERUPT_ATTACHMENT + "/**").addResourceLocations("file:" + uploadPath);
    }

}
