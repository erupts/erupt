package xyz.erupt.annotation.cube;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.sub_field.sub_edit.VL;

import java.lang.annotation.*;

/**
 * @author YuePeng
 * date 2025/11/2 20:53
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Parameter {

    String code();

    String title();

    String description() default "";

    FieldType type() default FieldType.STRING;

    boolean hidden() default false;

    @Comment("type == FieldType.STRING")
    VL[] vl() default {};

}
