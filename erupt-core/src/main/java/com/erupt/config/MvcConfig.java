package com.erupt.config;

import com.erupt.constant.RestPath;
import com.erupt.interceptor.LoginInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by liyuepeng on 10/31/18.
 */
@Configuration
public class MvcConfig extends WebMvcConfigurationSupport {

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        gsonHttpMessageConverter.setGson(gson);
        converters.add(gsonHttpMessageConverter);
        super.configureMessageConverters(converters);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns(RestPath.ERUPT_API + "/**");
        super.addInterceptors(registry);
    }

//    @Bean
//    public HttpMessageConverters customConverters() {
//        Collection<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
//        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
//        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
//        gsonHttpMessageConverter.setGson(gson);
//        messageConverters.add(gsonHttpMessageConverter);
//        return new HttpMessageConverters(true, messageConverters);
//    }

}
