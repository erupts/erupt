package xyz.erupt.core.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.erupt.annotation.sub_field.View;

/**
 * One column total asked of a data source: the view column and the aggregate declared
 * by {@code @View(statistic = ...)}.
 *
 * @author YuePeng
 */
@Getter
@AllArgsConstructor
public class Aggregate {

    // property path the source queries, e.g. "amount" or "customer.level"
    private final String path;

    // key the result is reported under: the table column index, e.g. "amount" or "customer_level"
    private final String key;

    private final View.Statistic statistic;

}
