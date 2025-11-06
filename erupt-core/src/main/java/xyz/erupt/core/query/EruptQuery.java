package xyz.erupt.core.query;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.query.Sort;

import java.util.List;

/**
 * erupt通用数据对象
 */
@Getter
@Setter
@Builder
public class EruptQuery {

    private List<Condition> conditions;

    private List<String> conditionStrings;

    private List<Sort> sort;

}
