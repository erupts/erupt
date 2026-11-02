package xyz.erupt.test.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * A bean whose @Value fields the config center must keep current.
 */
@Getter
@Component
public class ConfigDemoBean {

    @Value("${demo.greeting:hello}")
    private String greeting;

    @Value("${demo.limit:1}")
    private int limit;

}
