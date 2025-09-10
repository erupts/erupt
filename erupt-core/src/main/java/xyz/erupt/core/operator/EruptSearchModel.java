package xyz.erupt.core.operator;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/9/4 23:12
 */
@Getter
@Setter
public class EruptSearchModel {

    private String field;

    private OperatorType operatorType;

    private String operator;

    private Object value;

    public static String convertCondition(List<List<EruptSearchModel>> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<String> orConditions = new ArrayList<>();
        for (List<EruptSearchModel> andGroup : conditions) {
            if (andGroup == null || andGroup.isEmpty()) {
                continue;
            }
            List<String> andConditions = new ArrayList<>();
            for (EruptSearchModel search : andGroup) {
                if (search == null || search.getField() == null) {
                    continue;
                }
                Class<? extends Enum> operatorClass = search.getOperatorType().operatorClass;
                @SuppressWarnings("unchecked")
                OperatorExpr operatorExpr = (OperatorExpr) Enum.valueOf(operatorClass, search.getOperator());
                String condition = operatorExpr.expr(search.getField(), search.getValue());
                andConditions.add(condition);
            }
            if (!andConditions.isEmpty()) {
                orConditions.add("(" + String.join(" AND ", andConditions) + ")");
            }
        }
        if (!orConditions.isEmpty()) {
            sb.append(" AND (").append(String.join(" OR ", orConditions)).append(")");
        }
        return sb.toString();
    }

}
