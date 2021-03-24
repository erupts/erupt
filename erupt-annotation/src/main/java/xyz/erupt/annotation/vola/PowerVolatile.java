package xyz.erupt.annotation.vola;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.config.VolatileFun;
import xyz.erupt.annotation.sub_erupt.Power;

/**
 * @author YuePeng
 * date 2020-01-07
 */
@Deprecated
public class PowerVolatile implements VolatileFun<Power, Erupt> {

    @Override
    public Void exec(Power power, Erupt erupt) {
        return null;
    }

}
