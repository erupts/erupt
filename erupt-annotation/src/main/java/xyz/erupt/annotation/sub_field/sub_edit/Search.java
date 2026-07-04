package xyz.erupt.annotation.sub_field.sub_edit;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.QueryExpression;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
public @interface Search {

    boolean value() default true;

    @Comment("Whether the field is required")
    boolean notNull() default false;

    @Comment("Default query operator, used when the request condition does not carry one; AUTO resolves by edit type")
    QueryExpression operator() default QueryExpression.AUTO;

}
