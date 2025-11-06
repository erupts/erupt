package xyz.erupt.sample.job;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xyz.erupt.job.handler.EruptJobHandler;
import xyz.erupt.linq.lambda.LambdaSee;

@Slf4j
@Getter
public class TestJobHandler implements EruptJobHandler {

    @Override
    public String name() {
        return "Test Scheduled";
    }

    @Override
    public String exec(String code, String param) {
        log.info("A task was executed.");
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
