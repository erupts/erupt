package xyz.erupt.core.operator;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Map;

/**
 * @author YuePeng
 * date 2025/8/7 11:09
 */
public interface OperatorExpr {

    default String expr(String field, Object value, Map<String, Object> parameter) {
        throw new RuntimeException("Require Override expr()");
    }

    default String placeholder() {
        return RandomStringUtils.randomAlphabetic(8);
    }

}
