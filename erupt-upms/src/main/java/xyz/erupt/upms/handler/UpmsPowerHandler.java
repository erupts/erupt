package xyz.erupt.upms.handler;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.PowerHandler;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.invoke.PowerInvoke;
import xyz.erupt.upms.enums.EruptFunPermissions;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.util.UPMSUtil;

import javax.annotation.Resource;

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
    private EruptUserService eruptUserService;

    @Override
    public void handler(PowerObject power) {
        power.setAdd(powerOff(EruptFunPermissions.ADD));
        power.setDelete(powerOff(EruptFunPermissions.DELETE));
        power.setEdit(powerOff(EruptFunPermissions.EDIT));
        power.setViewDetails(powerOff(EruptFunPermissions.VIEW_DETAIL));
        power.setExport(powerOff(EruptFunPermissions.EXPORT));
        power.setImportable(powerOff(EruptFunPermissions.IMPORTABLE));
    }

    public boolean powerOff(EruptFunPermissions eruptFunPermissions) {
        return null != eruptUserService.getEruptMenuByValue(UPMSUtil.getEruptFunPermissionsCode(MetaContext.getErupt().getName(), eruptFunPermissions));
    }

}
