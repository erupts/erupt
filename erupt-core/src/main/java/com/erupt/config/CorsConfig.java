package com.erupt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 网络请求配置
 * Created by liyuepeng on 10/17/18.
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allow.domian}")
    public String allowDomain;

    @Value("${cors.allow.header}")
    public String allowHeader;

    @Value("${cors.allow.method}")
    public String allowMethod;

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin(allowDomain); // 允许任何域名使用
        corsConfiguration.addAllowedHeader(allowHeader); // 允许任何头
        corsConfiguration.addAllowedMethod(allowMethod); // 允许任何方法（post、get等）
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig()); // 对接口配置跨域设置
        return new CorsFilter(source);
    }


}
