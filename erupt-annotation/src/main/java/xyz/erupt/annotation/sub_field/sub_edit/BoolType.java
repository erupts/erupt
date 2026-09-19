package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.constant.AnnotationValue;

/**
 * @author YuePeng
 * date 2018-09-18.
 */
public @interface BoolType {

    String trueText() default AnnotationValue.Y;

    String falseText() default AnnotationValue.N;

    @Comment("Form widget; AUTO renders a switch when the field is notNull (two-state), otherwise radio buttons")
    Type type() default Type.AUTO;

    enum Type {
        @Comment("Switch when notNull, radio otherwise")
        AUTO,
        @Comment("Radio buttons; keeps an explicit unset state")
        RADIO,
        @Comment("Switch; an unset value is submitted as false")
        SWITCH,
    }
}
