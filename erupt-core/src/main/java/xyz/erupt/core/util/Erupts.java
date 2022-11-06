package xyz.erupt.core.util;

import org.apache.commons.lang3.RandomStringUtils;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.PowerInvoke;
import xyz.erupt.core.view.EruptModel;

import java.util.function.Function;

/**
 * @author YuePeng
 * date 2021/7/15 08:17
 */
public class Erupts {

    public static void powerLegal(EruptModel eruptModel, Function<PowerObject, Boolean> function, String errorMessage) {
        powerLegal(function.apply(PowerInvoke.getPowerObject(eruptModel)), errorMessage);
    }

    public static void powerLegal(EruptModel eruptModel, Function<PowerObject, Boolean> function) {
        powerLegal(eruptModel, function, null);
    }

    public static void powerLegal(Boolean bool) {
        powerLegal(bool, null);
    }

    public static void powerLegal(Boolean bool, String message) {
        if (!Boolean.TRUE.equals(bool)) throw new EruptNoLegalPowerException(message);
    }

    public static void requireFalse(boolean bool, String message) {
        requireTrue(!bool, message);
    }

    public static void requireTrue(boolean bool, String message) {
        if (!bool) throw new EruptWebApiRuntimeException(message);
    }

    public static <T> void requireNonNull(T t, String message) {
        if (t == null) throw new EruptWebApiRuntimeException(message);
    }

    public static void requireNull(Object obj, String message) {
        if (obj != null) throw new EruptWebApiRuntimeException(message);
    }

    public static String generateCode() {
        return generateCode(8);
    }

    public static String generateCode(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

}
