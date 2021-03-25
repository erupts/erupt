package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

import java.beans.Transient;

/**
 * @author YuePeng
 * date 2018-10-10.
 */
public @interface InputType {

    @Comment("最大输入长度")
    int length() default 255;

    String type() default "text";

    @Comment("是否整行显示")
    boolean fullSpan() default false;

    @Transient
    @Comment("对提交内容进行正则校验")
    String regex() default "";

    VL[] prefix() default {};

    VL[] suffix() default {};
}
