package xyz.erupt.core.service;

import lombok.SneakyThrows;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import xyz.erupt.core.annotation.EruptScan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author YuePeng
 * date 2020-09-09
 */
public class EruptApplication implements ImportBeanDefinitionRegistrar {

    private static Class<?> primarySource;

    private static final Set<String> scanPackage = new HashSet<>();


    public static Class<?> getPrimarySource() {
        return primarySource;
    }

    public static String[] getScanPackage() {
        return scanPackage.toArray(new String[0]);
    }

    @SneakyThrows
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Class<?> clazz = ClassUtils.forName(importingClassMetadata.getClassName(), ClassUtils.getDefaultClassLoader());
        if (null != clazz.getAnnotation(SpringBootApplication.class)) {
            primarySource = clazz;
        }
        EruptScan eruptScan = clazz.getAnnotation(EruptScan.class);
        if (eruptScan.value().length == 0) {
            scanPackage.add(clazz.getPackage().getName());
        } else {
            scanPackage.addAll(Arrays.asList(eruptScan.value()));
        }
//        if (eruptScan.value().length == 0) {
//            if (primarySource.getPackage().getName().startsWith(EruptConst.BASE_PACKAGE)) {
//                scanPackage = new String[1];
//                scanPackage[0] = EruptConst.BASE_PACKAGE;
//            } else {
//                scanPackage = new String[2];
//                scanPackage[0] = primarySource.getPackage().getName();
//                scanPackage[1] = EruptConst.BASE_PACKAGE;
//            }
//        } else {
//            scanPackage = eruptScan.value();
//        }
    }
}
