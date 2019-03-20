package xyz.erupt.core.handler;

import xyz.erupt.annotation.fun.ConditionHandler;

/**
 * Created by liyuepeng on 11/5/18.
 */
public class SimpleConditionHandler implements ConditionHandler {

    @Override
    public String handler(String condition) {
        return condition.replace("@abc@","7");
    }
}
