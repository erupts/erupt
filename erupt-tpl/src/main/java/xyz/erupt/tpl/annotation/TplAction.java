package xyz.erupt.tpl.annotation;

import java.lang.annotation.*;

/**
 * 修饰方法必须返回Map<String,Object>类型
 *
 * @author liyuepeng
 * @date 2020-02-24
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface TplAction {

    String mapping() default "";

}
