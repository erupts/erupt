package xyz.erupt.annotation.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用 SpEL 表达式校验注解信息，用来确定是否序列化，表达式必须返回布尔类型，
 *
 * @author YuePeng
 * date 2020-02-21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Match {

    /**
     * 预注入变量值：
     * value : 当前注解变量
     * item :  父级注解变量
     *
     * @return 表达式
     */
    String value();
}
