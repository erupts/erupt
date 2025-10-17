package xyz.erupt.core.operator;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/8/7 11:09
 */
@Getter
@AllArgsConstructor
public enum OperatorStringType implements OperatorExpr {

    EQ {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s = :%s", field, placeholder);
        }
    }, NEQ {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value);
            return String.format("%s != :%s", field, placeholder);
        }
    }, LIKE {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, "%" + value + "%");
            return String.format("%s like :%s", field, placeholder);
        }
    }, NOT_LIKE {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, "%" + value + "%");
            return String.format("%s not like :%s", field, placeholder);
        }
    }, START_WITH {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, value + "%");
            return String.format("%s like :%s", field, placeholder);
        }
    }, END_WITH {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, "%" + value);
            return String.format("%s like :%s", field, placeholder);
        }
    }, IN {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            Collection<?> collection = (Collection<?>) value;
            List<String> conditions = new ArrayList<>();
            for (Object object : collection) {
                String placeholder = this.placeholder();
                parameter.put(placeholder, "%" + object + "%");
                conditions.add(field + " like :" + placeholder);
            }
            return "(" + String.join(" or ", conditions) + ")";
        }
    }, NOT_IN {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            Collection<?> collection = (Collection<?>) value;
            List<String> conditions = new ArrayList<>();
            for (Object object : collection) {
                String placeholder = this.placeholder();
                parameter.put(placeholder, "%" + object + "%");
                conditions.add(field + " not like :" + placeholder);
            }
            return "(" + String.join(" and ", conditions) + ")";
        }
    }, NULL {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            return String.format("%s is null", field);
        }
    }, NOT_NULL {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            return String.format("%s is not null", field);
        }
    },


}
