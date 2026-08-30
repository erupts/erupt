package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * @author YuePeng
 * date 2024
 */
public @interface ColorType {

    @Comment("Allow the alpha (transparency) channel")
    boolean alpha() default false;

    @Comment("Preset color swatches shown in the picker panel, e.g. {\"#F5222D\", \"#1890FF\"}")
    String[] presets() default {};

    @Comment("Show the color value text next to the swatch")
    boolean showText() default true;
}
