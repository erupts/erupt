package xyz.erupt.core.operator;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author YuePeng
 * date 2025/8/7 11:09
 */
@Getter
@AllArgsConstructor
public enum OperatorNumberType implements DbOperatorExpr {

    EQ("等于") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s = %s", field, value);
        }
    },
    NEQ("不等于") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s <> %s", field, value);
        }
    },
    GT("大于") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s > %s", field, value);
        }
    },
    LT("小于") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s < %s", field, value);
        }
    },
    EGT("大于等于") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s >= %s", field, value);
        }
    },
    ELT("小于等于") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s <= %s", field, value);
        }
    },
    RANGE("区间") {
        @Override
        public String expr(String field, Object value) {
            List<Object> s = (List) value;
            return String.format("%s between %s and %s", field, s.get(0), s.get(1));
        }
    },
    NULL("为空") {
        @Override
        public String expr(String field, Object value) {
            return OperatorStringType.NULL.expr(field, value);
        }
    },
    NOT_NULL("非空") {
        @Override
        public String expr(String field, Object value) {
            return OperatorStringType.NOT_NULL.expr(field, value);
        }
    };

    //名称
    private final String name;

}
