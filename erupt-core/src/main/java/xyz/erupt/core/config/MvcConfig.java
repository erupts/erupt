package xyz.erupt.core.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.util.unit.DataSize;
import xyz.erupt.annotation.config.SkipSerialize;

import javax.servlet.MultipartConfigElement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author liyuepeng
 * @date 10/31/18.
 */
@Configuration
public class MvcConfig {

    @Bean
    public HttpMessageConverters customConverters() {
        Collection<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        gsonHttpMessageConverter.setGson(gson);
        messageConverters.add(gsonHttpMessageConverter);
        return new HttpMessageConverters(true, messageConverters);
    }


    @Bean
    public Gson gson() {
        return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                SkipSerialize skip = f.getAnnotation(SkipSerialize.class);
                return null != skip;
            }

            @Override
            public boolean shouldSkipClass(Class<?> incomingClass) {
                return false;
            }
        }).setDateFormat("yyyy-MM-dd HH:mm:ss").serializeNulls().create();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //  单个数据大小
        factory.setMaxFileSize(DataSize.parse("102400MB"));
        factory.setMaxRequestSize(DataSize.parse("102400MB"));
        return factory.createMultipartConfig();
    }


}
