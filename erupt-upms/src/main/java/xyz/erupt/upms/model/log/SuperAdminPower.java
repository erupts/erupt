package xyz.erupt.upms.model.log;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.PowerHandler;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2021/8/20 14:44
 */
@Component
public class SuperAdminPower implements PowerHandler {

    @Resource
    private EruptUserService eruptUserService;

    @Override
    public void handler(PowerObject power) {
        if (eruptUserService.getCurrentEruptUser().getIsAdmin()) {
            power.setDelete(true);
        }
    }
}
