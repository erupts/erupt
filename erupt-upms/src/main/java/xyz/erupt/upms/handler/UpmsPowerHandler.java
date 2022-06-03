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
import java.util.Map;

/**
 * 全局菜单权限控制
 *
 * @author YuePeng
 * date 2021/3/16 00:15
 */
@Service
public class UpmsPowerHandler implements PowerHandler {

    static {
        PowerInvoke.registerPowerHandler(UpmsPowerHandler.class);
    }

    @Resource
    private EruptUserService eruptUserService;

    @Override
    public void handler(PowerObject power) {
        Map<String, Boolean> permissionMap = eruptUserService.getEruptMenuValuesMap();
        if (power.isAdd()) {
            power.setAdd(powerOff(EruptFunPermissions.ADD, permissionMap));
        }
        if (power.isDelete()) {
            power.setDelete(powerOff(EruptFunPermissions.DELETE, permissionMap));
        }
        if (power.isEdit()) {
            power.setEdit(powerOff(EruptFunPermissions.EDIT, permissionMap));
        }
        if (power.isViewDetails()) {
            power.setViewDetails(powerOff(EruptFunPermissions.VIEW_DETAIL, permissionMap));
        }
        if (power.isExport()) {
            power.setExport(powerOff(EruptFunPermissions.EXPORT, permissionMap));
        }
        if (power.isImportable()) {
            power.setImportable(powerOff(EruptFunPermissions.IMPORTABLE, permissionMap));
        }
    }

    private boolean powerOff(EruptFunPermissions eruptFunPermissions, Map<String, Boolean> permissionMap) {
        return permissionMap.containsKey(UPMSUtil.getEruptFunPermissionsCode(MetaContext.getErupt().getName(), eruptFunPermissions).toLowerCase());
    }

}
