package xyz.erupt.core.operator;

/**
 * @author YuePeng
 * date 2025/8/7 11:09
 */
public interface DbOperatorExpr {

    default String expr(String field, Object value) {
        throw new RuntimeException("Require Override expr()");
    }

}
