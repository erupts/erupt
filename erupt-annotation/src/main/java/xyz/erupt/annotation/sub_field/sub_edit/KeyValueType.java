package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;

/**
 * Key-value pair editor. The field is either a String holding a JSON object such as
 * {@code {"timeout":"30","retry":"3"}}, or a {@code Map<String, String>} mapped to a JSON column
 * ({@code @JdbcTypeCode(SqlTypes.JSON)}) that receives the object directly. Keys and values are edited as
 * text, rows with an empty key are dropped on save.
 *
 * @author YuePeng
 */
public @interface KeyValueType {

    @Comment("Placeholder of the key column; empty uses the built-in translated text")
    String keyPlaceholder() default "";

    @Comment("Placeholder of the value column; empty uses the built-in translated text")
    String valuePlaceholder() default "";

    @Comment("Maximum number of pairs, 0 for unlimited")
    int max() default 0;

    @Comment("Fixed key suggestions offered while typing a key, e.g. {\"Content-Type\", \"Authorization\"}")
    String[] keys() default {};
}
