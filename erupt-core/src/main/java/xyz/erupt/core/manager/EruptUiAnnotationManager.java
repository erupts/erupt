package xyz.erupt.core.manager;

import lombok.Getter;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author YuePeng
 * date 2025/2/22 00:52
 */
public class EruptUiAnnotationManager {

    @Getter
    private static Set<Class<? extends Annotation>> annotations = new HashSet<>();

    public static void register(Class<? extends Annotation> annotation) {
        annotations.add(annotation);
    }

}
