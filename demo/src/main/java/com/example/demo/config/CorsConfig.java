package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*"); // 允许域名使用
        corsConfiguration.addAllowedHeader("*"); // 允许头
        corsConfiguration.addAllowedMethod("*"); // 允许方法（post、get等）
        source.registerCorsConfiguration("/**", corsConfiguration); // 对接口配置跨域设置
        return new CorsFilter(source);
    }


}
