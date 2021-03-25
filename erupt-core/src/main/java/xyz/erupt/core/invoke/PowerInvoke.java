package xyz.erupt.core.invoke;

import xyz.erupt.annotation.fun.PowerHandler;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2021/3/16 00:07
 */
public class PowerInvoke {

    private static final List<Class<? extends PowerHandler>> powerHandlerStack = new ArrayList<>();

    public static void RegisterPowerHandler(Class<? extends PowerHandler> powerHandler) {
        powerHandlerStack.add(powerHandler);
    }

    //动态获取erupt power值
    public static PowerObject getPowerObject(EruptModel eruptModel) {
        Power power = eruptModel.getErupt().power();
        PowerObject powerBean = new PowerObject(power);
        for (Class<? extends PowerHandler> ph : powerHandlerStack) {
            EruptSpringUtil.getBean(ph).handler(powerBean);
        }
        if (!power.powerHandler().isInterface()) {
            EruptSpringUtil.getBean(power.powerHandler()).handler(powerBean);
        }
        return powerBean;
    }
}
