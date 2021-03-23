package xyz.erupt.annotation;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.ToMap;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;

import java.beans.Transient;
import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface EruptField {

    @Comment("表格列配置")
    View[] views() default {};

    @Comment("编辑组件配置")
    Edit edit() default @Edit(title = "");

    @Transient
    @Comment("显示顺序，默认按照字段排列顺序排序")
    int sort() default 1000;

    @ToMap(key = "key")
    @Comment("自定义扩展参数")
    KV[] params() default {};
}
