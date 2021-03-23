package xyz.erupt.core.util;

import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.TypeFilter;
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

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (EruptSpringUtil.applicationContext == null) {
            EruptSpringUtil.applicationContext = applicationContext;
        }
    }

    //获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //通过class获取Bean.
    @SneakyThrows
    public static <T> T getBean(Class<T> clazz) {
        if (null != clazz.getDeclaredAnnotation(Component.class)
                || null != clazz.getDeclaredAnnotation(Service.class)
                || null != clazz.getDeclaredAnnotation(Repository.class)
                || null != clazz.getDeclaredAnnotation(RestController.class)
                || null != clazz.getDeclaredAnnotation(Controller.class)) {
            return getApplicationContext().getBean(clazz);
        } else {
            return clazz.newInstance();
        }
    }

    public static <T> T getBean(T t) {
        return (T) getBean(t.getClass());
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

    //注册bean
//    public static void registerBean(Class<?> clazz, String name, Map<String, String> refBeans, Map<String, Object> propertyValues) {
//        //将applicationContext转换为ConfigurableApplicationContext
//        ConfigurableApplicationContext configurableApplicationContext =
//                (ConfigurableApplicationContext) applicationContext;
//        // 获取bean工厂并转换为DefaultListableBeanFactory
//        DefaultListableBeanFactory defaultListableBeanFactory =
//                (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
//        // 通过BeanDefinitionBuilder创建bean定义
//        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
//        // 设置依赖容器
//        if (null != refBeans) {
//            for (Map.Entry<String, String> entry : refBeans.entrySet()) {
//                beanDefinitionBuilder.addPropertyReference(entry.getKey(), entry.getValue());
//            }
//        }
//        if (null != propertyValues) {
//            for (Map.Entry<String, Object> entry : propertyValues.entrySet()) {
//                beanDefinitionBuilder.addPropertyValue(entry.getKey(), entry.getValue());
//            }
//        }
//        //删除bean.
//        //defaultListableBeanFactory.removeBeanDefinition("testService");
//        // 注册bean
//        defaultListableBeanFactory.registerBeanDefinition(name, beanDefinitionBuilder.getRawBeanDefinition());
//    }

    /**
     * 按照相对应的规则查找所有匹配类
     *
     * @param packages    包名
     * @param typeFilters 匹配规则
     * @param consumer    consumer lambda
     */
    public static void scannerPackage(String[] packages, TypeFilter[] typeFilters, Consumer<Class<?>> consumer) {
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