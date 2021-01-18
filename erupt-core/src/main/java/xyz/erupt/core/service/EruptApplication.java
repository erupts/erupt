package xyz.erupt.core.service;

import lombok.SneakyThrows;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.EruptConst;

/**
 * @author liyuepeng
 * @date 2020-09-09
 */
public class EruptApplication implements ImportBeanDefinitionRegistrar {

    private static Class<?> primarySource;

    private static String[] scanPackage;

    public static Class<?> getPrimarySource() {
        return primarySource;
    }

    public static String[] getScanPackage() {
        return scanPackage;
    }

    @SneakyThrows
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        primarySource = ClassUtils.forName(importingClassMetadata.getClassName(), ClassUtils.getDefaultClassLoader());
        EruptScan eruptScan = primarySource.getAnnotation(EruptScan.class);
        if (eruptScan.value().length == 0) {
            if (primarySource.getPackage().getName().startsWith(EruptConst.BASE_PACKAGE)) {
                scanPackage = new String[1];
                scanPackage[0] = EruptConst.BASE_PACKAGE;
            } else {
                scanPackage = new String[2];
                scanPackage[0] = primarySource.getPackage().getName();
                scanPackage[1] = EruptConst.BASE_PACKAGE;
            }
        } else {
            scanPackage = eruptScan.value();
        }
    }
}
