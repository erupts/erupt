package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.TagsFetchHandler;

import java.beans.Transient;

/**
 * A TAGS field binds to a {@code String} (tags joined by {@link #joinSeparator()}, a JSON array string
 * for {@code "[]"}) or to a {@code String[]} / {@code List<String>}, which travels as a JSON array and
 * ignores the separator. On JPA, store the array or list as JSON so the dialect picks the column type
 * (jsonb, json, or a character column where the database has no JSON type):
 * <pre>
 * &#64;JdbcTypeCode(SqlTypes.JSON)
 * &#64;EruptField(views = &#64;View(title = "Stack"), edit = &#64;Edit(title = "Stack", type = EditType.TAGS))
 * private List&lt;String&gt; stack;
 * </pre>
 * Such a field is not searchable by tag (a JSON column takes no LIKE), so give it {@code search = false}.
 */
public @interface TagsType {

    @Comment("Separator character when storing multiple tags")
    String joinSeparator() default "[]";

    @Comment("Whether custom tags are allowed")
    boolean allowExtension() default true;

    @Comment("Maximum number of tags")
    int maxTagCount() default 9999;

    @Comment("List of selectable tags")
    String[] tags() default {};

    @Transient
    @Comment("This configuration is accessible from fetchHandler")
    String[] fetchHandlerParams() default {};

    @Comment("Dynamically fetched list of selectable tags")
    Class<? extends TagsFetchHandler>[] fetchHandler() default {};
}
