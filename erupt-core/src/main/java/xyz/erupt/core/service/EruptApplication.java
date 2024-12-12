package xyz.erupt.core.service;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.EruptConst;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author YuePeng
 * date 2020-09-09
 */
@Slf4j
public class EruptApplication implements ImportBeanDefinitionRegistrar {

    @Getter
    private static Class<?> primarySource;

    private static final Set<String> scanPackage = new HashSet<>();

    public static String[] getScanPackage() {
        return scanPackage.toArray(new String[0]);
    }

    @SneakyThrows
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Class<?> clazz = ClassUtils.forName(importingClassMetadata.getClassName(), ClassUtils.getDefaultClassLoader());
        Optional.ofNullable(clazz.getAnnotation(SpringBootApplication.class)).ifPresent(it -> primarySource = clazz);
        EruptScan eruptScan = clazz.getAnnotation(EruptScan.class);
        try {
            Class.forName("org.springframework.boot.devtools.RemoteUrlPropertyExtractor");
            log.error("spring-boot-devtools 与 erupt 同时存在会出现各种奇怪的问题，建议移除该依赖！！！");
        } catch (ClassNotFoundException ignored) {
        }
        if (eruptScan.value().length == 0) {
            scanPackage.add(clazz.getPackage().getName());
        } else {
            Stream.of(eruptScan.value()).filter(pack -> !pack.equals(EruptConst.BASE_PACKAGE)).forEach(scanPackage::add);
        }
    }
}
