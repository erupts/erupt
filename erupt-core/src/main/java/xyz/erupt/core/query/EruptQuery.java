package xyz.erupt.core.query;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * erupt通用数据对象
 */
@Getter
@Setter
@Builder
public class EruptQuery {
    List<Condition> conditions;

    List<String> conditionStrings;

    String orderBy;
}
