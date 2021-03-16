package xyz.erupt.upms.handler;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.PowerHandler;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.invoke.PowerInvoke;

/**
 * @author liyuepeng
 * @date 2021/3/16 00:15
 */
@Service
public class UpmsPowerHandler implements PowerHandler {

    static {
        PowerInvoke.RegisterPowerHandler(UpmsPowerHandler.class);
    }

    @Override
    public void handler(PowerObject power) {

    }

}
