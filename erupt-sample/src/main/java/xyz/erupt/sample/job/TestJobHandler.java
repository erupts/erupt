package xyz.erupt.sample.job;

import lombok.extern.slf4j.Slf4j;
import xyz.erupt.job.handler.EruptJobHandler;

@Slf4j
public class TestJobHandler implements EruptJobHandler {

    @Override
    public String name() {
        return "测试定时任务";
    }

    @Override
    public String exec(String code, String param) {
        log.info("执行了一次定义任务");
        return null;
    }

    @Override
    public String cron() {
        return EruptJobHandler.super.cron();
    }

    @Override
    public String param() {
        return "{}";
    }

}
