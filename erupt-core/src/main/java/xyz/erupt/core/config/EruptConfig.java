package xyz.erupt.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * @author liyuepeng
 * @date 2020-08-07
 */
@Configuration
public class EruptConfig {

    @Bean
    public ScriptEngine eruptScriptEngine() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        return scriptEngineManager.getEngineByName("JavaScript");
    }

}
