package xyz.erupt.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

/**
 * Created by liyuepeng on 2019-10-18.
 */
@Configuration
public class TemplateConfig {


    @Bean
    public TemplateEngine templateEngine() {
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setCacheable(false);
        resolver.setTemplateMode(TemplateMode.HTML);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addTemplateResolver(resolver);
        return templateEngine;
    }
}
