package xyz.erupt.core.operator;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * @author YuePeng
 * date 2025/8/7 11:09
 */
@Getter
@AllArgsConstructor
public enum OperatorReferenceType implements OperatorExpr {

    EQ {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s in (:%s)", field, placeholder);
        }
    },
    NEQ {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s not in (:%s)", field, placeholder);
        }
    },

    NULL{
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            return OperatorStringType.NULL.expr(field, value, parameter);
        }
    },
    NOT_NULL {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            return OperatorStringType.NOT_NULL.expr(field, value, parameter);
        }
    };

}
