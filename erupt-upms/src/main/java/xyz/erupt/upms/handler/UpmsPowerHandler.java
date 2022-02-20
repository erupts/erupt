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
        if (power.isAdd()) {
            power.setAdd(powerOff(EruptFunPermissions.ADD));
        }
        if (power.isDelete()) {
            power.setDelete(powerOff(EruptFunPermissions.DELETE));
        }
        if (power.isEdit()) {
            power.setEdit(powerOff(EruptFunPermissions.EDIT));
        }
        if (power.isViewDetails()) {
            power.setViewDetails(powerOff(EruptFunPermissions.VIEW_DETAIL));
        }
        if (power.isExport()) {
            power.setExport(powerOff(EruptFunPermissions.EXPORT));
        }
        if (power.isImportable()) {
            power.setImportable(powerOff(EruptFunPermissions.IMPORTABLE));
        }
    }

    private boolean powerOff(EruptFunPermissions eruptFunPermissions) {
        return null != eruptUserService.getEruptMenuByValue(UPMSUtil.getEruptFunPermissionsCode(MetaContext.getErupt().getName(), eruptFunPermissions));
    }

}
