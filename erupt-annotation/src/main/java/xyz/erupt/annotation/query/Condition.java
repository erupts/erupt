package xyz.erupt.annotation.query;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.config.QueryExpression;

@Getter
@Setter
public class Condition {

    /** Field name; dotted for a reference field, such as org.name */
    private String key;

    /** Comparison operator */
    private QueryExpression expression;

    /** Value to compare with; a two element array for RANGE, an array for IN */
    private Object value;

    public Condition(String key, Object value, QueryExpression expression) {
        this.key = key;
        this.value = value;
        this.expression = expression;
    }

}
