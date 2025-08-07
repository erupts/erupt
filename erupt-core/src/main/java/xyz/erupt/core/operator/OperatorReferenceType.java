package xyz.erupt.core.operator;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/8/7 11:09
 */
@Getter
@AllArgsConstructor
public enum OperatorReferenceType implements DbOperatorExpr {

    EQ("包含") {
        @Override
        public String expr(String field, Object value) {
            if (value instanceof List) {
                List<Object> s = (List<Object>) value;
                return String.format("%s in (%s)", field, geneJoinIn(s));
            } else if (value instanceof Number) {
                return String.format("%s = %s", field, Integer.parseInt(value.toString()));
            } else {
                return String.format("%s = '%s'", field, value);
            }
        }
    },
    NEQ("不包含") {
        @Override
        public String expr(String field, Object value) {
            if (value instanceof List) {
                List<Object> s = (List<Object>) value;
                return String.format("%s not in (%s)", field, geneJoinIn(s));
            } else if (value instanceof Number) {
                return String.format("%s <> %s", field, Integer.parseInt(value.toString()));
            } else {
                return String.format("%s <> '%s'", field, value);
            }
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

    public static String geneJoinIn(List<Object> list) {
        List<String> newList = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Number) {
                newList.add(o.toString());
            } else {
                newList.add("'" + o + "'");
            }
        }
        return String.join(",", newList);
    }

}
