package xyz.erupt.handler;

import xyz.erupt.annotation.fun.ConditionHandler;

/**
 * Created by liyuepeng on 11/5/18.
 */
public class SimpleConditionHandler implements ConditionHandler {

    @Override
    public String placeHolderStr() {
        return "abc";
    }

    @Override
    public String placeHolderData() {
        return 1 + "";
    }
}
