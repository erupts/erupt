package xyz.erupt.cloud.server.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.server.model.CloudNode;
import xyz.erupt.cloud.server.service.EruptNodeMicroservice;
import xyz.erupt.cloud.server.service.NodePowerService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Map;

/**
 * @author YuePeng
 * date 2022/3/5 19:18
 */
@RestController
public class EruptServerApi {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptUserService eruptUserService;

    @Resource
    private EruptNodeMicroservice eruptNodeMicroservice;

    @Resource
    private NodePowerService nodePowerService;

    @GetMapping(CloudRestApiConst.ERUPT_POWER)
    public PowerObject eruptPower(@RequestParam("eruptName") String eruptName, @RequestParam("nodeName") String nodeName) {
        return nodePowerService.userPower(nodeName + EruptConst.DOT + eruptName);
    }

    @GetMapping(CloudRestApiConst.NODE_CONFIG + "/{nodeName}")
    public String getNodeConfig(@PathVariable("nodeName") String nodeName, @RequestHeader(CloudCommonConst.HEADER_ACCESS_TOKEN) String accessToken) {
        return eruptDao.lambdaQuery(CloudNode.class).eq(CloudNode::getNodeName, nodeName)
                .eq(CloudNode::getAccessToken, accessToken).oneSelect(CloudNode::getConfig);
    }

    @GetMapping(CloudRestApiConst.NODE_GROUP_CONFIG + "/{nodeName}")
    public String getNodeGroupConfig(@PathVariable("nodeName") String nodeName, @RequestHeader(CloudCommonConst.HEADER_ACCESS_TOKEN) String accessToken) {
        CloudNode cloudNode = eruptDao.lambdaQuery(CloudNode.class)
                .eq(CloudNode::getNodeName, nodeName)
                .eq(CloudNode::getAccessToken, accessToken).one();
        if (null == cloudNode) return null;
        return cloudNode.getCloudNodeGroup().getConfig();
    }


    //User information
    @GetMapping(CloudRestApiConst.ERUPT_USER_INFO + "/{nodeName}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public MetaUserinfo userinfo(@PathVariable("nodeName") String nodeName, @RequestHeader(CloudCommonConst.HEADER_ACCESS_TOKEN) String accessToken) {
        eruptNodeMicroservice.findNodeByAppName(nodeName, accessToken);
        return eruptUserService.getSimpleUserInfo();
    }

}
