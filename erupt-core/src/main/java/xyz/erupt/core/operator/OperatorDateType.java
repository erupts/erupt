package xyz.erupt.core.operator;


import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.TypeUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/8/7 11:09
 */
@Getter
@AllArgsConstructor
public enum OperatorDateType implements OperatorExpr {

    TODAY {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            return FUTURE_DAYS.expr(field, 0, parameter);
        }
    },
    FEW_DAYS {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            int days = TypeUtil.fetchInt(value);
            String placeholder1 = this.placeholder();
            parameter.put(placeholder1, LocalDate.now().minusDays(days).atStartOfDay()
                    .format(DateTimeFormatter.ofPattern(DateUtil.DATE_TIME)));
            String placeholder2 = this.placeholder();
            parameter.put(placeholder2, LocalDate.now().atStartOfDay()
                    .format(DateTimeFormatter.ofPattern(DateUtil.DATE_TIME)));
            return String.format("%s between :%s and :%s", field, placeholder1, placeholder2);
        }
    },
    FUTURE_DAYS {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            int days = TypeUtil.fetchInt(value);
            String placeholder1 = this.placeholder();
            parameter.put(placeholder1, LocalDate.now().plusDays(1)
                    .format(DateTimeFormatter.ofPattern(DateUtil.DATE)));
            String placeholder2 = this.placeholder();
            parameter.put(placeholder2, LocalDate.now().plusDays(days).atTime(LocalTime.MAX)
                    .format(DateTimeFormatter.ofPattern(DateUtil.DATE_TIME)));
            return String.format("%s between :%s and :%s", field, placeholder1, placeholder2);
        }
    },
    RANGE {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            List<?> s = (ArrayList<?>) value;
            String placeholder1 = this.placeholder();
            parameter.put(placeholder1, s.get(0));
            String placeholder2 = this.placeholder();
            parameter.put(placeholder2, s.get(1) + TIME_END);
            return String.format("%s between :%s and :%s", field, placeholder1, placeholder2);
        }
    }, GT {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, OperatorDateType.parseDate(value.toString()));
            return String.format("%s > :%s", field, placeholder);
        }
    }, LT {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, OperatorDateType.parseDate(value.toString()) + TIME_END);
            return String.format("%s < :%s", field, placeholder);
        }
    }, EGT {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, OperatorDateType.parseDate(value.toString()));
            return String.format("%s >= :%s", field, placeholder);
        }
    }, ELT {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            String placeholder = this.placeholder();
            parameter.put(placeholder, OperatorDateType.parseDate(value.toString()) + TIME_END);
            return String.format("%s <= :%s", field, placeholder);
        }
    }, NULL {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            return OperatorStringType.NULL.expr(field, value, null);
        }
    }, NOT_NULL {
        @Override
        public String expr(String field, Object value, Map<String, Object> parameter) {
            return OperatorStringType.NOT_NULL.expr(field, value, null);
        }
    };

    public static String parseDate(String date) {
        return date;
    }

    public static final String TIME_END = " 23:59:59.999999999";

}
