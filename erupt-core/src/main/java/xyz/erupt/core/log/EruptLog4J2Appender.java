package xyz.erupt.core.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.nio.charset.StandardCharsets;

public class EruptLog4J2Appender implements EruptAppender {

    @Override
    public void init() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = context.getConfiguration();
        LoggerConfig logger = configuration.getRootLogger();
        PatternLayout layout = PatternLayout.newBuilder().withCharset(StandardCharsets.UTF_8)
                .withConfiguration(configuration).withPattern("%d %t %p %X{TracingMsg} %c - %m%n").build();
        Appender appender = new Appender(logger.getFilter(), layout);
        appender.start();
        configuration.addAppender(appender);
        logger.addAppender(appender, logger.getLevel(), logger.getFilter());
        context.updateLoggers(configuration);
    }

    static class Appender extends AbstractAppender {

        Appender(Filter filter, Layout<String> layout) {
            super("erupt", filter, layout, false, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            Formatter formatter = new Formatter()
                    .timestamp(event.getTimeMillis())
                    .space()
                    .value(event.getLevel().toString())
                    .value(" --- [ ")
                    .value(event.getThreadName())
                    .value("]")
                    .value(event.getLoggerName())
                    .value(": ")
                    .value(event.getMessage().getFormattedMessage())
                    .throwable(event.getThrown());
            EruptLogManager.log(formatter.toString());
        }
    }
}
