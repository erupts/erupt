package com.erupt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


@SpringBootApplication
@SpringBootConfiguration
public class EruptCoreApplication{

    public static void main(String[] args) {
        SpringApplication.run(EruptCoreApplication.class, args);
    }



    //    @Bean
//    public HibernateJpaSessionFactoryBean sessionFactory() {
//        return new HibernateJpaSessionFactoryBean();
//    }
}