package xyz.erupt.core.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * @author liyuepeng
 * @date 1/24/19.
 */
@Component
public class EruptSpringUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (EruptSpringUtil.applicationContext == null) {
            EruptSpringUtil.applicationContext = applicationContext;
        }
    }

    //获取applicationContext
    private static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz) {
        try {
            return getApplicationContext().getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    public static <T> T getBean(String path, Class<T> clazz) throws ClassNotFoundException {
        return clazz.cast(getBean(Class.forName(path)));
    }

    /**
     * 按照相对应的规则查找所有匹配类
     *
     * @param packages    包名
     * @param typeFilters 匹配规则
     * @param consumer    consumer lambda
     */
    public void scannerPackage(String[] packages, TypeFilter[] typeFilters, Consumer<Class<?>> consumer) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        for (TypeFilter filter : typeFilters) {
            scanner.addIncludeFilter(filter);
        }
        for (String pack : packages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(pack)) {
                try {
                    Class<?> clazz = Class.forName(bd.getBeanClassName());
                    consumer.accept(clazz);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}