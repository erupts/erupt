package xyz.erupt.core.naming;

import org.springframework.stereotype.Component;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.service.EruptCoreService;

import java.lang.reflect.Method;

/**
 * @author YuePeng
 * date 2021/5/7 10:28
 */
@Component
public class EruptOperateConfig implements EruptRecordOperate.DynamicConfig {

    @Override
    public String naming(String desc, String menuName, String eruptName, Method method) {
        EruptRouter eruptRouter = method.getAnnotation(EruptRouter.class);
        if (null != eruptRouter && eruptRouter.verifyType() == EruptRouter.VerifyType.ERUPT) {
            String prefix = desc + " | ";
            if (null != menuName) {
                return prefix + menuName;
            } else if (null != EruptCoreService.getErupt(eruptName)) {
                return prefix + EruptCoreService.getErupt(eruptName).getErupt().name();
            } else {
                return prefix + eruptName;
            }
        } else {
            throw new RuntimeException("Incorrect use " + EruptOperateConfig.class.getSimpleName());
        }
    }

}