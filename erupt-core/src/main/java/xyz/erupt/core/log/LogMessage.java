package xyz.erupt.core.log;

import lombok.Getter;

@Getter
public class LogMessage {

    private static long increment = 0L;

    private final long num;

    private final String content;

    public LogMessage(String content) {
        this.content = content;
        num = ++increment;
    }

}
