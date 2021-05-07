package xyz.erupt.core.naming;

import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.service.EruptCoreService;

import java.lang.reflect.Method;

/**
 * @author YuePeng
 * date 2021/5/7 10:28
 */
public class EruptOperateConfig implements EruptRecordOperate.DynamicConfig {

    @Override
    public String naming(String desc, String eruptName, Method method) {
        EruptRouter eruptRouter = method.getAnnotation(EruptRouter.class);
        if (null != eruptRouter && eruptRouter.verifyType() == EruptRouter.VerifyType.ERUPT) {
            return desc + " | " + EruptCoreService.getErupt(eruptName).getErupt().name();
        } else {
            throw new RuntimeException("Incorrect use " + EruptOperateConfig.class.getSimpleName());
        }
    }

}