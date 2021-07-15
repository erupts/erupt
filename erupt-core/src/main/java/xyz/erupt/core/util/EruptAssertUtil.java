package xyz.erupt.core.util;

import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.invoke.PowerInvoke;
import xyz.erupt.core.view.EruptModel;

import java.util.function.Function;

/**
 * @author YuePeng
 * date 2021/7/15 08:17
 */
public class EruptAssertUtil {

    public static void powerLegal(EruptModel eruptModel, Function<PowerObject, Boolean> function) {
        Boolean bool = function.apply(PowerInvoke.getPowerObject(eruptModel));
        if (null == bool || !bool) {
            throw new EruptNoLegalPowerException();
        }
    }


}
