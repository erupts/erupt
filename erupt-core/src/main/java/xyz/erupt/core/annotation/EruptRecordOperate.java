package xyz.erupt.core.annotation;

import xyz.erupt.annotation.config.Comment;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * @author YuePeng
 * date 2020-05-26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Comment("Record operation logs")
public @interface EruptRecordOperate {

    @Comment("Operation name")
    String value();

    Class<? extends DynamicConfig> dynamicConfig() default DynamicConfig.class;

    interface DynamicConfig {
        String naming(String desc, String menuName, String eruptName, Method method);

        default boolean canRecord(String eruptName, Method method) {
            return true;
        }
    }

}
