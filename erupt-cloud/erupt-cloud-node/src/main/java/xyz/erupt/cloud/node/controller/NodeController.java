package xyz.erupt.cloud.node.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;

/**
 * @author YuePeng
 * date 2022/3/1 23:17
 */
@RestController
public class NodeController {

    @GetMapping(CloudRestApiConst.NODE_HEALTH)
    public void health() {
    }

}
