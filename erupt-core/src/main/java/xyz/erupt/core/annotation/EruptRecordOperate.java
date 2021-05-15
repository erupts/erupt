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
@Comment("记录操作日志")
public @interface EruptRecordOperate {

    @Comment("操作名称")
    String value();

    Class<? extends DynamicConfig> dynamicConfig() default DynamicConfig.class;

    interface DynamicConfig {
        String naming(String desc, String eruptName, Method method);

        default boolean canRecord(String eruptName, Method method) {
            return true;
        }
    }

}
