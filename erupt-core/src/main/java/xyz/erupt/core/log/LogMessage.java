package xyz.erupt.core.log;

import lombok.Getter;

/**
 * @author YuePeng
 * date 2024/6/27 21:58
 */
@Getter
public class LogMessage {

    private static long increment = 0L;

    /** Sequence number; pass it back as offset to fetch newer lines */
    private final long num;

    /** Log line */
    private final String content;

    public LogMessage(String content) {
        this.content = content;
        num = ++increment;
    }

    public static long getMax() {
        return increment;
    }
}
