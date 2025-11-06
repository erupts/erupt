package xyz.erupt.core.util;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Consumer;

/**
 * @author YuePeng
 * date 1/24/19.
 */
@Component
public class EruptSpringUtil implements ApplicationContextAware {

    @Getter
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        if (EruptSpringUtil.applicationContext == null) {
            EruptSpringUtil.applicationContext = applicationContext;
        }
    }

    @SneakyThrows
    public static <T> T getBean(Class<T> clazz) {
        if (null != clazz.getDeclaredAnnotation(Component.class)
                || null != clazz.getDeclaredAnnotation(Service.class)
                || null != clazz.getDeclaredAnnotation(Repository.class)
                || null != clazz.getDeclaredAnnotation(RestController.class)
                || null != clazz.getDeclaredAnnotation(Controller.class)) {
            return getApplicationContext().getBean(clazz);
        } else {
            return clazz.getDeclaredConstructor().newInstance();
        }
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getProperty(String key, Class<T> clazz) {
        return getApplicationContext().getEnvironment().getProperty(key, clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    //根据类路径获取bean
    public static <T> T getBeanByPath(String path, Class<T> clazz) throws ClassNotFoundException {
        return clazz.cast(getBean(Class.forName(path)));
    }

    /**
     * Scan all matching classes according to the corresponding rules
     *
     * @param packages    package names
     * @param typeFilters matching rule
     * @param consumer    consumer lambda
     */
    @SneakyThrows
    public static void scannerPackage(String[] packages, TypeFilter[] typeFilters, Consumer<Class<?>> consumer) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        for (TypeFilter filter : typeFilters) {
            scanner.addIncludeFilter(filter);
        }
        for (String pack : packages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(pack)) {
                consumer.accept(Class.forName(bd.getBeanClassName()));
            }
        }
    }
}