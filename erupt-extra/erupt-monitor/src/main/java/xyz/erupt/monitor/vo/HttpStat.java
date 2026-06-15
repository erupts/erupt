package xyz.erupt.monitor.vo;

import lombok.Getter;

import java.util.concurrent.atomic.LongAdder;

/**
 * Per-endpoint HTTP statistics snapshot.
 *
 * @author YuePeng
 * date 2026/6/15
 */
@Getter
public class HttpStat {

    private final String uri;
    private final long count;
    private final long avgMs;
    private final long maxMs;
    private final long errorCount;

    public HttpStat(String uri, LongAdder count, LongAdder totalMs, LongAdder errorCount, long maxMs) {
        this.uri = uri;
        this.count = count.sum();
        this.avgMs = this.count > 0 ? totalMs.sum() / this.count : 0;
        this.maxMs = maxMs;
        this.errorCount = errorCount.sum();
    }

}
