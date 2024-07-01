package xyz.erupt.core.log;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.erupt.core.constant.EruptConst;


/**
 * @author YuePeng
 * date 2024/6/27 21:58
 */
public class EruptLogbackAppender implements EruptAppender {

    @Override
    public void init() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        Appender appender = new Appender();
        appender.setContext(context);
        appender.setName(EruptConst.ERUPT);
        appender.start();
        logger.addAppender(appender);
    }

    public static class Appender extends UnsynchronizedAppenderBase<ILoggingEvent> {

        @Override
        protected void append(ILoggingEvent event) {
            IThrowableProxy proxy = event.getThrowableProxy();
            Formatter formatter = new Formatter()
                    .timestamp(event.getTimeStamp())
                    .space()
                    .level(event.getLevel().toString())
                    .value(" --- [")
                    .thread(event.getThreadName())
                    .value("] ")
                    .name(event.getLoggerName())
                    .value(" : ")
                    .value(event.getFormattedMessage());
            if (proxy instanceof ThrowableProxy) {
                formatter.throwable(((ThrowableProxy) proxy).getThrowable());
            }
            EruptLogManager.log(formatter.toString());
        }

    }

}
