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

    public static void registerPowerHandler(Class<? extends PowerHandler> powerHandler) {
        powerHandlerStack.add(powerHandler);
    }

    //The erupt power value is processed dynamically
    public static PowerObject getPowerObject(EruptModel eruptModel) {
        Power power = eruptModel.getErupt().power();
        PowerObject powerBean = new PowerObject(power);
        if (eruptModel.getErupt().authVerify()) {
            powerHandlerStack.forEach(ph -> EruptSpringUtil.getBean(ph).handler(powerBean));
            if (!power.powerHandler().isInterface()) EruptSpringUtil.getBean(power.powerHandler()).handler(powerBean);
        }
        PowerObject processorMaster = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).power();
        if (!processorMaster.isAdd()) powerBean.setAdd(false);
        if (!processorMaster.isDelete()) powerBean.setDelete(false);
        if (!processorMaster.isEdit()) powerBean.setEdit(false);
        if (!processorMaster.isQuery()) powerBean.setQuery(false);
        if (!processorMaster.isViewDetails()) powerBean.setViewDetails(false);
        if (!processorMaster.isExport()) powerBean.setExport(false);
        if (!processorMaster.isImportable()) powerBean.setImportable(false);
        return powerBean;
    }
}
