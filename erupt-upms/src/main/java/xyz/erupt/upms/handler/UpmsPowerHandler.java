package xyz.erupt.upms.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.PowerHandler;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.invoke.PowerInvoke;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.enums.MenuLimitEnum;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptSessionService;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * 全局菜单权限控制
 *
 * @author YuePeng
 * date 2021/3/16 00:15
 */
@Service
public class UpmsPowerHandler implements PowerHandler {

    static {
        PowerInvoke.RegisterPowerHandler(UpmsPowerHandler.class);
    }

    @Resource
    private EruptSessionService eruptSessionService;

    @Resource
    private EruptContextService eruptContextService;

    @Override
    public void handler(PowerObject power) {
        Optional.ofNullable(eruptContextService.getCurrentEruptMenu()).ifPresent(eruptMenu -> {
            this.powerOff(eruptMenu.getPowerOff(), power);
            Optional.of(eruptSessionService.get(SessionKey.ROLE_POWER + eruptContextService.getCurrentToken()))
                    .ifPresent(it -> this.powerOff(it.toString(), power));
        });
    }

    private void powerOff(String powerStr, PowerObject power) {
        if (StringUtils.isNotBlank(powerStr)) {
            if (powerStr.contains(MenuLimitEnum.NO_ADD.name())) {
                power.setAdd(false);
            }
            if (powerStr.contains(MenuLimitEnum.NO_DELETE.name())) {
                power.setDelete(false);
            }
            if (powerStr.contains(MenuLimitEnum.NO_EDIT.name())) {
                power.setEdit(false);
            }
            if (powerStr.contains(MenuLimitEnum.NO_QUERY.name())) {
                power.setQuery(false);
            }
            if (powerStr.contains(MenuLimitEnum.NO_EXPORT.name())) {
                power.setExport(false);
            }
            if (powerStr.contains(MenuLimitEnum.NO_IMPORT.name())) {
                power.setImportable(false);
            }
        }
    }

}
