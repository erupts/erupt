package xyz.erupt.core.operator;

/**
 * @author YuePeng.li
 * @date 2022-06-15 6:48 PM
 */
public interface DbOperatorExpr {

    default String expr(String field, Object value) {
        throw new RuntimeException("Require Override expr()");
    }

}
