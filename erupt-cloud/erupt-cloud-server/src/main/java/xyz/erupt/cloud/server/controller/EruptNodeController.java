package xyz.erupt.cloud.server.controller;

import org.springframework.web.bind.annotation.*;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.server.base.CloudServerConst;
import xyz.erupt.cloud.server.node.NodeManager;
import xyz.erupt.upms.annotation.EruptMenuAuth;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2023/6/9 22:10
 */
@RestController
@RequestMapping(CloudRestApiConst.ERUPT_CLOUD_API)
public class EruptNodeController {

    @Resource
    private NodeManager nodeManager;

    //移除实例
    @GetMapping("/remove-instance/{nodeName}")
    @EruptMenuAuth(CloudServerConst.CLOUD_NODE_MANAGER_PERMISSION)
    public void removeInstance(@PathVariable String nodeName, @RequestParam("instance") String instance) {
        nodeManager.removeNodeInstance(nodeName, instance);
    }

}
