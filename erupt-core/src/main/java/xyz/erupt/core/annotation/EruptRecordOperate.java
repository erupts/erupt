package xyz.erupt.core.annotation;

import xyz.erupt.annotation.config.Comment;

import java.lang.annotation.*;

/**
 *
 * @author liyuepeng
 * @date 2020-05-26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Comment("记录操作日志")
public @interface EruptRecordOperate {

    @Comment("操作名称")
    String desc();

}
