package xyz.erupt.cloud.server.controller;

import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.server.model.CloudNode;
import xyz.erupt.cloud.server.service.EruptNodeMicroservice;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.enums.EruptFunPermissions;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptSessionService;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.upms.util.UPMSUtil;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2022/3/5 19:18
 */
@RestController
public class EruptServerApi {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptSessionService eruptSessionService;

    @Resource
    private EruptContextService eruptContextService;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptNodeMicroservice eruptNodeMicroservice;

    @GetMapping(CloudRestApiConst.ERUPT_POWER)
    public PowerObject eruptPower(@RequestParam String eruptName, @RequestParam String nodeName) {
        PowerObject powerObject = new PowerObject();
        List<String> values = eruptSessionService.getMapKeys(SessionKey.MENU_VALUE_MAP + eruptContextService.getCurrentToken());
        Map<String, Boolean> permissionMap = values.stream().collect(Collectors.toMap(it -> it, it -> true));
        String name = nodeName + EruptConst.DOT + eruptName;
        powerObject.setQuery(permissionMap.containsKey(name.toLowerCase()));
        powerObject.setAdd(powerOff(EruptFunPermissions.ADD, permissionMap, name));
        powerObject.setDelete(powerOff(EruptFunPermissions.DELETE, permissionMap, name));
        powerObject.setAdd(powerOff(EruptFunPermissions.ADD, permissionMap, name));
        powerObject.setEdit(powerOff(EruptFunPermissions.EDIT, permissionMap, name));
        powerObject.setExport(powerOff(EruptFunPermissions.EXPORT, permissionMap, name));
        powerObject.setImportable(powerOff(EruptFunPermissions.IMPORTABLE, permissionMap, name));
        powerObject.setViewDetails(powerOff(EruptFunPermissions.VIEW_DETAIL, permissionMap, name));
        return powerObject;
    }

    @GetMapping(CloudRestApiConst.NODE_CONFIG + "/{nodeName}")
    public String getNodeConfig(@PathVariable String nodeName, @RequestHeader(CloudCommonConst.HEADER_ACCESS_TOKEN) String accessToken) {
        return eruptDao.lambdaQuery(CloudNode.class).eq(CloudNode::getNodeName, nodeName)
                .eq(CloudNode::getAccessToken, accessToken).oneSelect(CloudNode::getConfig);
    }

    @GetMapping(CloudRestApiConst.NODE_GROUP_CONFIG + "/{nodeName}")
    public String getNodeGroupConfig(@PathVariable String nodeName, @RequestHeader(CloudCommonConst.HEADER_ACCESS_TOKEN) String accessToken) {
        return (String) eruptDao.getEntityManager()
                .createQuery("select cloudNodeGroup.config from CloudNode where nodeName = :nodeName and accessToken = :accessToken")
                .setParameter("nodeName", nodeName)
                .setParameter("accessToken", accessToken).getSingleResult();
    }


    //用户信息
    @GetMapping(CloudRestApiConst.ERUPT_USER_INFO + "/{nodeName}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public MetaUserinfo userinfo(@PathVariable String nodeName, @RequestHeader(CloudCommonConst.HEADER_ACCESS_TOKEN) String accessToken) {
        eruptNodeMicroservice.findNodeByAppName(nodeName, accessToken);
        return eruptUserService.getSimpleUserInfo();
    }

    private boolean powerOff(EruptFunPermissions eruptFunPermissions, Map<String, Boolean> permissionMap, String name) {
        return permissionMap.containsKey(UPMSUtil.getEruptFunPermissionsCode(name, eruptFunPermissions).toLowerCase());
    }

}
