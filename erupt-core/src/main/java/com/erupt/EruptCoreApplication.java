package com.erupt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
public class EruptCoreApplication{

    public static void main(String[] args) {
        SpringApplication.run(EruptCoreApplication.class, args);
    }

}