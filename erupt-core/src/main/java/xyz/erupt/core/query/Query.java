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

    List<String> list;

    List<Order> orders;

    public static void geneCondition(String a, String b) {

    }
}
