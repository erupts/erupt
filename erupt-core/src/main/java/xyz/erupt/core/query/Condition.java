package xyz.erupt.core.query;

import lombok.Data;

@Data
public class Condition {

    private String key;

    private Object value;

    private Expression expression;

    public Condition(String key, Object value, Expression expression) {
        this.key = key;
        this.value = value;
        this.expression = expression;
    }

    public enum Expression {
        EQ, RANGE, IN
    }

}
