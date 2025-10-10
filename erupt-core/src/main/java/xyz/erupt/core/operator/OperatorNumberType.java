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

    EQ("等于") {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s = :%s", field, placeholder);
        }
    },
    NEQ("不等于") {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s <> :%s", field, placeholder);
        }
    },
    GT("大于") {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s > :%s", field, placeholder);
        }
    },
    LT("小于") {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s < :%s", field, placeholder);
        }
    },
    EGT("大于等于") {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s >= :%s", field, placeholder);
        }
    },
    ELT("小于等于") {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s <= :%s", field, placeholder);
        }
    },
    RANGE("区间") {
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
    NULL("为空") {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            return OperatorStringType.NULL.expr(field, value, parameter);
        }
    },
    NOT_NULL("非空") {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            return OperatorStringType.NOT_NULL.expr(field, value, parameter);
        }
    };

    //名称
    private final String name;

}
