package xyz.erupt.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.TypeFilter;

import java.util.List;
import java.util.function.Consumer;

/**
 * 扫描相关工具类
 * Created by liyuepeng on 10/11/18.
 */
public class ScannerUtil {


    /**
     * 扫描指定包下的所有匹配类
     * 核心功能由Spring提供
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
