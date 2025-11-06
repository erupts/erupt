package xyz.erupt.core.operator;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/8/7 11:09
 */
@Getter
@AllArgsConstructor
public enum OperatorNumberType implements OperatorExpr {

    EQ {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s = :%s", field, placeholder);
        }
    },
    NEQ {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s <> :%s", field, placeholder);
        }
    },
    GT {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s > :%s", field, placeholder);
        }
    },
    LT {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s < :%s", field, placeholder);
        }
    },
    EGT {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s >= :%s", field, placeholder);
        }
    },
    ELT {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s <= :%s", field, placeholder);
        }
    },
    RANGE {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            List<?> s = (List<?>) value;
            String placeholder1 = this.placeholder();
            String placeholder2 = this.placeholder();
            parameter.put(placeholder1, s.get(0));
            parameter.put(placeholder2, s.get(1));
            return String.format("%s between :%s and :%s", field, placeholder1, placeholder2);
        }
    },
    NULL {
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
