package xyz.erupt.core.operator;


import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.TypeUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * @author YuePeng
 * date 2025/8/7 11:09
 */
@Getter
@AllArgsConstructor
public enum OperatorDateType implements DbOperatorExpr {
    TODAY("今天") {
        @Override
        public String expr(String field, Object value) {
            return FUTURE_DAYS.expr(field, 0);
        }
    },
    FEW_DAYS("过去 N 天") {
        @Override
        public String expr(String field, Object value) {
            int days = TypeUtil.fetchInt(value);
            // 起始：N 天前 00:00:00
            String start = LocalDate.now()
                    .minusDays(days)
                    .atStartOfDay()
                    .format(DateTimeFormatter.ofPattern(DateUtil.DATE_TIME));
            // 结束：今天 00:00:00（不包含今天）
            String end = LocalDate.now()
                    .atStartOfDay()
                    .format(DateTimeFormatter.ofPattern(DateUtil.DATE_TIME));
            return String.format("%s between '%s' and '%s'", field, start, end);
        }
    },
    FUTURE_DAYS("未来 N 天") {
        @Override
        public String expr(String field, Object value) {
            int days = TypeUtil.fetchInt(value);
            // 起始：明天 00:00:00
            String start = LocalDate.now()
                    .plusDays(1)
                    .format(DateTimeFormatter.ofPattern(DateUtil.DATE));
            // 结束：第 N 天 23:59:59
            String end = LocalDate.now()
                    .plusDays(days)
                    .atTime(LocalTime.MAX)          // 23:59:59.999999999
                    .format(DateTimeFormatter.ofPattern(DateUtil.DATE_TIME));
            return String.format("%s between '%s' and '%s'", field, start, end);
        }
    },
    RANGE("区间") {
        @Override
        public String expr(String field, Object value) {
            ArrayList<String> s = (ArrayList<String>) value;
            return String.format("%s between '%s' and '%s'", field, s.get(0), s.get(1) + " 23:59:59");
        }
    }, GT("大于") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s > '%s'", field, OperatorDateType.parseDate(value.toString()));
        }
    }, LT("小于") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s < '%s'", field, OperatorDateType.parseDate(value.toString()) + " 23:59:59");
        }
    }, EGT("大于等于") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s >= '%s'", field, OperatorDateType.parseDate(value.toString()));
        }
    }, ELT("小于等于") {
        @Override
        public String expr(String field, Object value) {
            return String.format("%s <= '%s'", field, OperatorDateType.parseDate(value.toString()) + " 23:59:59");
        }
    }, NULL("为空") {
        @Override
        public String expr(String field, Object value) {
            return OperatorStringType.NULL.expr(field, value);
        }
    }, NOT_NULL("非空") {
        @Override
        public String expr(String field, Object value) {
            return OperatorStringType.NOT_NULL.expr(field, value);
        }
    };

    //名称
    private final String name;

    public static String parseDate(String date) {
        return date;
    }

}
