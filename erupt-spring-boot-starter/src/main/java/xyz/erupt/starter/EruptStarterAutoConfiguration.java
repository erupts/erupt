package xyz.erupt.starter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import xyz.erupt.core.service.EruptApplication;

import java.util.List;

/**
 * Applies @EruptScan and @EntityScan automatically for applications that only
 * depend on erupt-spring-boot-starter, so the main class no longer needs to
 * declare them by hand.
 *
 * @author YuePeng
 */
@Slf4j
@AutoConfiguration
@Import(EruptStarterAutoConfiguration.Registrar.class)
public class EruptStarterAutoConfiguration {

    static class Registrar implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(@NonNull AnnotationMetadata metadata, @NonNull BeanDefinitionRegistry registry) {
            EruptApplication.checkDevtools();
            if (!(registry instanceof BeanFactory beanFactory) || !AutoConfigurationPackages.has(beanFactory)) {
                return;
            }
            // Base packages derived from the @SpringBootApplication / @EnableAutoConfiguration class.
            List<String> basePackages = AutoConfigurationPackages.get(beanFactory);
            if (basePackages.isEmpty()) {
                return;
            }
            // Equivalent to @EntityScan(basePackages): make JPA discover the application's own entities.
            EntityScanPackages.register(registry, basePackages);
            // Equivalent to @EruptScan(basePackages): make Erupt discover @Erupt models and handlers.
            EruptApplication.addScanPackage(basePackages.toArray(new String[0]));
            // Locate the primary source so application level annotations (e.g. @EruptLogin) keep working.
            EruptApplication.setPrimarySource(findPrimarySource(basePackages));
        }

        // Scan base packages for the class annotated with @SpringBootApplication.
        private Class<?> findPrimarySource(List<String> basePackages) {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(SpringBootApplication.class));
            for (String basePackage : basePackages) {
                for (var candidate : scanner.findCandidateComponents(basePackage)) {
                    try {
                        return ClassUtils.forName(candidate.getBeanClassName(), ClassUtils.getDefaultClassLoader());
                    } catch (ClassNotFoundException e) {
                        log.warn("Unable to resolve primary source candidate: {}", candidate.getBeanClassName());
                    }
                }
            }
            return null;
        }
    }
}
