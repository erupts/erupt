package xyz.erupt.flow.process.listener.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.process.listener.AfterCreateTaskListener;

/**
 * 监听器需要注册到spring
 */
@Component
@Slf4j
public class ConsoleListener implements AfterCreateTaskListener {
    @Override
    public void execute(OaTask task) {
        log.info("==> 有新任务{}", task.getId());
    }
}
