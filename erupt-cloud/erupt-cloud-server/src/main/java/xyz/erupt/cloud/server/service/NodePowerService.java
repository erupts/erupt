package xyz.erupt.cloud.server.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.enums.EruptFunPermissions;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.util.UPMSUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The current user's rights on a node's erupt, from the menu permissions of the session. The server enforces
 * them before forwarding (see {@link EruptRouter#power()}) and answers nodes that still ask for them.
 *
 * @author YuePeng
 * date 2026/10/14
 */
@Service
public class NodePowerService {

    @Resource
    private EruptSessionService eruptSessionService;

    @Resource
    private EruptContextService eruptContextService;

    /**
     * @param dottedErupt "nodeName.eruptName", the value of the erupt's menu
     */
    public PowerObject userPower(String dottedErupt) {
        Set<String> menus = new HashSet<>(eruptSessionService.getMapKeys(SessionKey.MENU_VALUE_MAP + eruptContextService.getCurrentToken()));
        PowerObject power = new PowerObject();
        power.setQuery(menus.contains(dottedErupt.toLowerCase()));
        power.setAdd(granted(menus, dottedErupt, EruptFunPermissions.ADD));
        power.setDelete(granted(menus, dottedErupt, EruptFunPermissions.DELETE));
        power.setEdit(granted(menus, dottedErupt, EruptFunPermissions.EDIT));
        power.setExport(granted(menus, dottedErupt, EruptFunPermissions.EXPORT));
        power.setImportable(granted(menus, dottedErupt, EruptFunPermissions.IMPORTABLE));
        power.setViewDetails(granted(menus, dottedErupt, EruptFunPermissions.VIEW_DETAIL));
        return power;
    }

    // At least one of the required rights; none required means the menu alone
    public static boolean allows(PowerObject power, EruptRouter.Power[] required) {
        return required.length == 0 || Arrays.stream(required).anyMatch(it -> switch (it) {
            case QUERY -> power.isQuery();
            case ADD -> power.isAdd();
            case EDIT -> power.isEdit();
            case DELETE -> power.isDelete();
            case VIEW_DETAILS -> power.isViewDetails();
            case EXPORT -> power.isExport();
            case IMPORTABLE -> power.isImportable();
        });
    }

    // The rights of the user, applied onto what the node's build model offers
    public static void restrict(PowerObject offered, PowerObject user) {
        offered.setAdd(offered.isAdd() && user.isAdd());
        offered.setEdit(offered.isEdit() && user.isEdit());
        offered.setDelete(offered.isDelete() && user.isDelete());
        offered.setViewDetails(offered.isViewDetails() && user.isViewDetails());
        offered.setExport(offered.isExport() && user.isExport());
        offered.setImportable(offered.isImportable() && user.isImportable());
    }

    private static boolean granted(Set<String> menus, String dottedErupt, EruptFunPermissions permission) {
        return menus.contains(UPMSUtil.getEruptFunPermissionsCode(dottedErupt, permission).toLowerCase());
    }

}
