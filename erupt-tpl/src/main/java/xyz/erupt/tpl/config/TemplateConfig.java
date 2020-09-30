package xyz.erupt.tpl.config;

import freemarker.template.Configuration;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * @author liyuepeng
 * @date 2019-10-18.
 */
@org.springframework.context.annotation.Configuration
public class TemplateConfig {

    @Bean
    public Configuration freeMarkerEngine() {
        Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_29);
        freemarkerConfig.setDefaultEncoding("utf-8");
        freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/");
//        freemarkerConfig.setDirectoryForTemplateLoading(new File(this.getClass().getResource("/").getPath()));
        return freemarkerConfig;
    }

    @Bean
    public TemplateEngine thymeleafEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setCacheable(false);
        resolver.setPrefix("/");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCheckExistence(true);
        resolver.setUseDecoupledLogic(true);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addTemplateResolver(resolver);
        return templateEngine;
    }
}
