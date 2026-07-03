package xyz.erupt.core.service;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.EruptConst;

import java.util.Collections;
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

    // Register additional packages to be scanned by Erupt.
    // Shared by @EruptScan and erupt-spring-boot-starter auto configuration.
    public static void addScanPackage(String... packages) {
        Collections.addAll(scanPackage, packages);
    }

    // Set the Spring Boot primary source, ignoring subsequent overrides.
    // The primary source carries application level annotations such as
    // @EruptLogin / @EruptCloudServer / @EruptAttachmentUpload.
    public static void setPrimarySource(Class<?> clazz) {
        if (null == primarySource) {
            primarySource = clazz;
        }
    }

    // spring-boot-devtools is incompatible with Erupt and triggers weird bugs.
    public static void checkDevtools() {
        try {
            Class.forName("org.springframework.boot.devtools.RemoteUrlPropertyExtractor");
            log.error("If  spring-boot-devtools  is on the classpath together with Erupt, weird bugs start to pop up.\n" +
                    "Just drop the devtools dependency—problem solved.");
        } catch (ClassNotFoundException ignored) {
        }
    }

    @SneakyThrows
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,@NonNull BeanDefinitionRegistry registry) {
        Class<?> clazz = ClassUtils.forName(importingClassMetadata.getClassName(), ClassUtils.getDefaultClassLoader());
        Optional.ofNullable(clazz.getAnnotation(SpringBootApplication.class)).ifPresent(it -> setPrimarySource(clazz));
        EruptScan eruptScan = clazz.getAnnotation(EruptScan.class);
        checkDevtools();
        if (eruptScan.value().length == 0) {
            scanPackage.add(clazz.getPackage().getName());
        } else {
            Stream.of(eruptScan.value()).filter(pack -> !pack.equals(EruptConst.BASE_PACKAGE)).forEach(scanPackage::add);
        }
    }
}
