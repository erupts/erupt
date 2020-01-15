package xyz.erupt.example.handler;

import xyz.erupt.annotation.fun.PowerHandler;

/**
 * @author liyuepeng
 * @date 2020-01-15
 */
public class PowerHandlerImpl implements PowerHandler {
    @Override
    public void handler(PowerBean power) {
        power.setDelete(false);
    }
}
