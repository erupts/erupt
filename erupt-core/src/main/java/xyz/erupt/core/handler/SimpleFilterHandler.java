package xyz.erupt.core.handler;

import xyz.erupt.annotation.fun.FilterHandler;

/**
 * Created by liyuepeng on 11/5/18.
 */
public class SimpleFilterHandler implements FilterHandler {

    @Override
    public String filter(String condition, String... params) {
        return condition.replace("@abc@", "7");
    }

}
