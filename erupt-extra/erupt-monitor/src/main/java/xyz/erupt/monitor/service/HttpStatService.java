package xyz.erupt.monitor.service;

import org.springframework.stereotype.Service;
import xyz.erupt.monitor.vo.HttpStat;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * In-memory per-endpoint HTTP statistics. Thread-safe, zero locking on the hot path.
 *
 * @author YuePeng
 * date 2026/6/15
 */
@Service
public class HttpStatService {

    private static final int MAX_ENDPOINTS = 1000;

    private static final class Entry {
        final LongAdder count = new LongAdder();
        final LongAdder totalMs = new LongAdder();
        final LongAdder errorCount = new LongAdder();
        volatile long maxMs;
    }

    private final ConcurrentHashMap<String, Entry> stats = new ConcurrentHashMap<>();

    public void record(String uri, long elapsedMs, int status) {
        Entry entry = stats.get(uri);
        if (entry == null) {
            if (stats.size() >= MAX_ENDPOINTS) return;
            entry = stats.computeIfAbsent(uri, k -> new Entry());
        }
        entry.count.increment();
        entry.totalMs.add(elapsedMs);
        if (elapsedMs > entry.maxMs) entry.maxMs = elapsedMs;
        if (status >= 400) entry.errorCount.increment();
    }

    public List<HttpStat> top(int n, String sortBy) {
        Comparator<HttpStat> comparator = switch (sortBy) {
            case "count" -> Comparator.comparingLong(HttpStat::getCount).reversed();
            case "error" -> Comparator.comparingLong(HttpStat::getErrorCount).reversed();
            default -> Comparator.comparingLong(HttpStat::getAvgMs).reversed();
        };
        return stats.entrySet().stream()
                .map(e -> new HttpStat(e.getKey(), e.getValue().count, e.getValue().totalMs, e.getValue().errorCount, e.getValue().maxMs))
                .sorted(comparator)
                .limit(n)
                .collect(Collectors.toList());
    }

    public void reset() {
        stats.clear();
    }

}
