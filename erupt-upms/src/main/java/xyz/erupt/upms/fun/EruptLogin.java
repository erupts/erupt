package xyz.erupt.upms.fun;

import xyz.erupt.annotation.config.Comment;

import java.lang.annotation.*;

/**
 * @author liyuepeng
 * @date 2021/2/13 20:10
 */
@Comment("自定义附件上传，需在spring boot入口类中修饰")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface EruptLogin {

    Class<? extends LoginProxy> value();

}
