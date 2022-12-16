package xyz.erupt.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xyz.erupt.core.annotation.EruptScan;

@SpringBootApplication
@EruptScan
public class EruptSampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(EruptSampleApplication.class, args);
	}

}
