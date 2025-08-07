package xyz.erupt.core.operator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.erupt.annotation.fun.VLModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/8/7 11:09
 */
@Getter
@AllArgsConstructor
public enum OperatorStringType implements DbOperatorExpr {

    EQ("等于") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s = '%s'", field, value);
        }
    }, NEQ("不等于") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s != '%s'", field, value);
        }
    }, LIKE("相似") {
        @Override
        public String expr(String field, Object value) {
            return field + " like " + "'%" + value + "%'";
        }
    }, NOT_LIKE("不相似") {
        @Override
        public String expr(String field, Object value) {
            return field + " not like " + "'%" + value + "%'";
        }
    }, START_WITH("以**开始") {
        @Override
        public String expr(String field, Object value) {
            return field + " like '" + value + "%'";
        }
    }, END_WITH("以**结尾") {
        @Override
        public String expr(String field, Object value) {
            return field + " like '%" + value + "'";
        }
    }, IN("包含于") {
        @Override
        public String expr(String field, Object value) {
            Collection<?> collection = (Collection<?>) value;
            List<String> conditions = new ArrayList<>();
            for (Object object : collection) {
                conditions.add(field + " like" + "'%" + object + "%'");
            }
            return "(" + String.join(" or ", conditions) + ")";
        }
    }, NOT_IN("不包含于") {
        @Override
        public String expr(String field, Object value) {
            Collection<?> collection = (Collection<?>) value;
            List<String> conditions = new ArrayList<>();
            for (Object object : collection) {
                conditions.add(field + " not like" + "'%" + object + "%'");
            }
            return "(" + String.join(" or ", conditions) + ")";
        }
    }, NULL("为空") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s is null", field);
        }
    }, NOT_NULL("非空") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s is not null", field);
        }
    },
    ;

    //名称
    private final String name;

    public static List<VLModel> fetchList() {
        List<VLModel> vlModels = new ArrayList<>();
        for (OperatorStringType value : OperatorStringType.values()) {
            vlModels.add(new VLModel(value.name(), value.getName()));
        }
        return vlModels;
    }

}
