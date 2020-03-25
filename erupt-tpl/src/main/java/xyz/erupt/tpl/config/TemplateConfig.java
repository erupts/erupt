package xyz.erupt.tpl.config;

import freemarker.template.Configuration;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.IOException;

/**
 * @author liyuepeng
 * @date 2019-10-18.
 */
@org.springframework.context.annotation.Configuration
public class TemplateConfig {

    @Bean
    public TemplateEngine thymeleafEngine() throws IOException {
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setCacheable(false);
        resolver.setPrefix(this.getClass().getResource("/").getPath());
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCheckExistence(true);
        resolver.setUseDecoupledLogic(true);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addTemplateResolver(resolver);
        return templateEngine;
    }

    @Bean
    public Configuration freeMarkerEngine() throws IOException {
        Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_29);
        freemarkerConfig.setDefaultEncoding("utf-8");
        freemarkerConfig.setDirectoryForTemplateLoading(new File(this.getClass().getResource("/").getFile()));
        return freemarkerConfig;
    }
}
