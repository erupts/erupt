package xyz.erupt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import xyz.erupt.core.annotation.EruptScan;

@SpringBootApplication
@EruptScan
@EntityScan
public class EruptApplication {

    public static void main(String[] args) {
        SpringApplication.run(EruptApplication.class, args);
    }

}
