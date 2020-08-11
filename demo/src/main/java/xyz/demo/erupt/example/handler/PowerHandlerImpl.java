package xyz.demo.erupt.example.handler;

import xyz.erupt.annotation.fun.PowerHandler;
import xyz.erupt.annotation.fun.PowerObject;

/**
 * @author liyuepeng
 * @date 2020-01-15
 */
public class PowerHandlerImpl implements PowerHandler {
    @Override
    public void handler(PowerObject power) {
        power.setDelete(false);
//        power.setDelete(false);
    }
}
