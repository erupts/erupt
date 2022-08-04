package xyz.erupt.jpa.config;

import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;
import xyz.erupt.jpa.support.CommentIntegrator;

import java.util.Collections;
import java.util.Map;

/**
 * @author YuePeng
 * date 2022/8/3 21:48
 */
@Component
public class HibernateConfig implements HibernatePropertiesCustomizer {

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.use_sql_comments", true);
        hibernateProperties.put("hibernate.integrator_provider",
                (IntegratorProvider) () -> Collections.singletonList(new CommentIntegrator()));
    }

}
