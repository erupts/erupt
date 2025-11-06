package xyz.erupt.cloud_server.docker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import xyz.erupt.core.annotation.EruptScan;

/**
 * @author Barcke
 * @version 1.0
 * projectName erupt
 * className Application
 * date 2024/12/10 10:40
 * slogan: Derived from life, yet surpassing life
 **/
@SpringBootApplication
@EruptScan
@EntityScan
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}