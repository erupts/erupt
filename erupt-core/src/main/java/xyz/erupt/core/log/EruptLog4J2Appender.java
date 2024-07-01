package xyz.erupt.core.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import xyz.erupt.core.constant.EruptConst;

import java.nio.charset.StandardCharsets;

/**
 * @author YuePeng
 * date 2024/6/27 21:58
 */
public class EruptLog4J2Appender implements EruptAppender {

    @Override
    public void init() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = context.getConfiguration();
        LoggerConfig logger = configuration.getRootLogger();
        PatternLayout layout = PatternLayout.newBuilder().withCharset(StandardCharsets.UTF_8)
                .withConfiguration(configuration).withPattern("%d %t %p %X{TracingMsg} %c - %m%n").build();
        AbstractAppender appender = getLog4j2Appender(logger, layout);
        configuration.addAppender(appender);
        logger.addAppender(appender, logger.getLevel(), logger.getFilter());
        context.updateLoggers(configuration);
    }

    private static AbstractAppender getLog4j2Appender(LoggerConfig logger, PatternLayout layout) {
        AbstractAppender appender = new AbstractAppender(EruptConst.ERUPT, logger.getFilter(), layout, false, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                EruptLogManager.log(new Formatter()
                        .timestamp(event.getTimeMillis())
                        .space()
                        .level(event.getLevel().toString())
                        .value(" --- [")
                        .thread(event.getThreadName())
                        .value("] ")
                        .name(event.getLoggerName())
                        .value("(" + event.getSource().getLineNumber() + ")")
                        .value(" : ")
                        .value(event.getMessage().getFormattedMessage())
                        .throwable(event.getThrown())
                        .toString()
                );
            }
        };
        appender.start();
        return appender;
    }

}
