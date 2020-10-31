package xyz.erupt.core.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * erupt通用数据对象
 */
@Data
@Builder
public class Query {
    List<Condition> conditions;

    List<String> conditionStrings;

    String order;
}
