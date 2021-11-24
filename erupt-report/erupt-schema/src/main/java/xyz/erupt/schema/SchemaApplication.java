package xyz.erupt.schema;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import xyz.erupt.core.annotation.EruptScan;

@Configuration
@ComponentScan
@EntityScan
@EruptScan
public class SchemaApplication {

}

