package xyz.erupt.cloud.node.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.node.config.EruptNodeProp;

import jakarta.annotation.Resource;

/**
 * @author YuePeng
 * date 2022/3/1 23:17
 */
@RestController
public class NodeController {

    @Resource
    private EruptNodeProp eruptNodeProp;

    @GetMapping(CloudRestApiConst.NODE_HEALTH)
    public String health() {
        return eruptNodeProp.getNodeName();
    }

}
