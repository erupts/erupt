package xyz.erupt.annotation.config;

public enum QueryExpression {
    // Sentinel: resolve to the edit-type default operator at runtime.
    // Only meaningful as the @Search.expression default, never as a real query condition.
    AUTO,
    EQ,
    NEQ,
    GT,
    GTE,
    LT,
    LTE,
    LIKE,
    NOT_LIKE,
    RANGE,
    IN,
    NOT_IN,
    NULL,
    NOT_NULL
}
