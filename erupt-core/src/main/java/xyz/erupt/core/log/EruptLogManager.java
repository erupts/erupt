package xyz.erupt.core.log;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xyz.erupt.core.prop.EruptProp;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * @author YuePeng
 * date 2024/6/27 21:58
 */
@Slf4j
@Component
public class EruptLogManager {

    @Resource
    private EruptProp eruptProp;

    @PostConstruct
    public void appender() {
        if (eruptProp.isLogTrack()) {
            ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
            String loggerFactoryClassName = loggerFactory.getClass().getName();
            if ("ch.qos.logback.classic.LoggerContext".equalsIgnoreCase(loggerFactoryClassName)) {
                new EruptLogbackAppender().init(); // logback
            } else if ("org.apache.logging.slf4j.Log4jLoggerFactory".equalsIgnoreCase(loggerFactoryClassName)) {
                new EruptLog4J2Appender().init();  // log4j2
            } else {
                log.error("EruptLog Unsupported log library :{}", loggerFactoryClassName);
            }
        }
    }

    private static final Deque<LogMessage> EVENT_QUEUE = new ArrayDeque<>();

    private static final int LOG_CACHE_SIZE = 2000;

    public static void log(String message) {
        if (EVENT_QUEUE.size() > LOG_CACHE_SIZE) EVENT_QUEUE.poll();
        EVENT_QUEUE.add(new LogMessage(message));
    }

    public static List<LogMessage> getEventQueue(Long size, Long offset) {
        List<LogMessage> result = new ArrayList<>();
        int i = 0;
        for (LogMessage message : EVENT_QUEUE) {
            if (null == offset) {
                result.add(message);
            } else if (message.getNum() > offset) {
                result.add(message);
            }
            if (++i > size + (null == offset ? 0 : offset)) {
                break;
            }
        }
        return result;
    }

}
