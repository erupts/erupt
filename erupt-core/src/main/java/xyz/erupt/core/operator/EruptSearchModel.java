package xyz.erupt.core.operator;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.core.constant.UpmsScope;

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

    // Used with UPMS OperatorType
    private UpmsScope upmsScope;

    private String operator;

    private Object value;

    public static SqlParams convertCondition(List<List<EruptSearchModel>> conditions) {
        SqlParams sqlParams = new SqlParams();
        if (conditions == null || conditions.isEmpty()) {
            return sqlParams;
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
                String condition = operatorExpr.expr(search.getField(), search.getValue(), sqlParams.getParams());
                andConditions.add(condition);
            }
            if (!andConditions.isEmpty()) {
                orConditions.add("(" + String.join(" AND ", andConditions) + ")");
            }
        }
        if (!orConditions.isEmpty()) {
            sb.append("(").append(String.join(" OR ", orConditions)).append(")");
        }
        sqlParams.setSql(sb.toString());
        return sqlParams;
    }

}
