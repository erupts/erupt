package xyz.erupt.core.log;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class EruptJULAppender extends Handler implements EruptAppender {

    @Override
    public void publish(LogRecord record) {
        EruptLogManager.log(new Formatter()
                .timestamp(record.getMillis())
                .space()
                .level(record.getLevel().toString())
                .value(" --- [")
                .thread(Thread.currentThread().getName())
                .value("] ")
                .name(record.getLoggerName())
                .value("(" + record.getSourceMethodName() + ":" + record.getSequenceNumber() + ")")
                .value(" : ")
                .value(record.getMessage())
                .throwable(record.getThrown())
                .toString()
        );
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }

    @Override
    public void init() {

    }

}
