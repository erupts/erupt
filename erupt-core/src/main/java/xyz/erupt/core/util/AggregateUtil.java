package xyz.erupt.core.util;

import xyz.erupt.core.query.Aggregate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * In-memory column statistics over row maps, with the same semantics a SQL source gives:
 * used by bean data sources and as the fallback for columns a source cannot aggregate
 * itself (transient fields filled by a DataProxy, for example).
 *
 * @author YuePeng
 */
public final class AggregateUtil {

    private AggregateUtil() {
    }

    public static Object compute(List<Map<String, Object>> rows, Aggregate aggregate) {
        List<Object> values = rows.stream()
                .map(r -> r.containsKey(aggregate.getKey()) ? r.get(aggregate.getKey()) : r.get(aggregate.getPath()))
                .filter(Objects::nonNull).collect(Collectors.toList());
        List<BigDecimal> numbers = values.stream().map(v -> {
            try {
                return new BigDecimal(String.valueOf(v));
            } catch (NumberFormatException e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        switch (aggregate.getStatistic()) {
            case COUNT:
                return (long) values.size();
            case DISTINCT_COUNT:
                return values.stream().map(String::valueOf).distinct().count();
            case SUM:
                return numbers.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            case AVG:
                return numbers.isEmpty() ? null : numbers.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(numbers.size()), 6, RoundingMode.HALF_UP);
            case MAX:
                return numbers.stream().max(Comparator.naturalOrder()).orElse(null);
            case MIN:
                return numbers.stream().min(Comparator.naturalOrder()).orElse(null);
            default:
                return null;
        }
    }

}
