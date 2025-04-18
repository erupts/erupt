package xyz.erupt.cloud.server.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.server.model.CloudNode;
import xyz.erupt.cloud.server.node.MetaNode;
import xyz.erupt.cloud.server.service.EruptNodeMicroservice;
import xyz.erupt.cloud.server.util.CloudServerUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * 客户端注册控制器
 *
 * @author YuePeng
 * date 2021/12/17 00:01
 */
@RestController
@AllArgsConstructor
@Slf4j
public class EruptMicroserviceController {

    private final EruptNodeMicroservice eruptNodeMicroservice;

    @PostMapping(CloudRestApiConst.REGISTER_NODE)
    public void registerNode(@RequestBody MetaNode metaNode, HttpServletRequest request, HttpServletResponse response) {
        CloudNode cloudNode = eruptNodeMicroservice.findNodeByAppName(metaNode.getNodeName(), metaNode.getAccessToken());
        if (!cloudNode.getStatus()) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            throw new RuntimeException(metaNode.getNodeName() + " prohibiting the registration");
        }
        Optional.ofNullable(CloudServerUtil.findEruptCloudServerAnnotation()).ifPresent(it -> it.registerNode(metaNode, request));
        for (String location : metaNode.getNodeAddress()) {
            if (!CloudServerUtil.nodeHealth(metaNode.getNodeName(), location)) {
                throw new RuntimeException("Unable to establish a connection with " + location);
            }
        }
        eruptNodeMicroservice.registerNode(cloudNode, metaNode);
    }

    //移除实例
    @PostMapping(CloudRestApiConst.REMOVE_INSTANCE_NODE)
    public void removeInstanceNode(@RequestParam String nodeName, @RequestParam String accessToken, HttpServletRequest request) {
        Optional.ofNullable(CloudServerUtil.findEruptCloudServerAnnotation()).ifPresent(it -> it.removeNode(nodeName, request));
        eruptNodeMicroservice.safeRemoveInstance(nodeName, accessToken);
    }

}
