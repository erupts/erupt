package xyz.erupt.cloud.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.common.model.RegisterAnswer;
import xyz.erupt.cloud.common.node.CloudProtocol;
import xyz.erupt.cloud.server.model.CloudNode;
import xyz.erupt.cloud.server.node.MetaNode;
import xyz.erupt.cloud.server.service.EruptNodeMicroservice;
import xyz.erupt.cloud.server.util.CloudServerUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Client registration controller
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
    public RegisterAnswer registerNode(@RequestBody MetaNode metaNode, HttpServletRequest request, HttpServletResponse response) {
        if (null == CloudProtocol.of(metaNode.getProtocolVersion())) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            throw new RuntimeException(metaNode.getNodeName() + " speaks protocol version " + metaNode.getProtocolVersion()
                    + ", this server speaks up to " + CloudProtocol.latest().wire());
        }
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
        // the newest version spoken here: a v2 node refuses to run against a server that answers less (PROTOCOL.md §12.2)
        return new RegisterAnswer(CloudProtocol.latest().wire());
    }

    //Remove instance. When the node reports its own locations (graceful shutdown) they are dropped
    //precisely; otherwise fall back to a survival poll.
    @PostMapping(CloudRestApiConst.REMOVE_INSTANCE_NODE)
    public void removeInstanceNode(@RequestParam("nodeName") String nodeName, @RequestParam("accessToken") String accessToken,
                                   @RequestParam(value = "locations", required = false) String locations, HttpServletRequest request) {
        Optional.ofNullable(CloudServerUtil.findEruptCloudServerAnnotation()).ifPresent(it -> it.removeNode(nodeName, request));
        List<String> locationList = (null == locations || locations.isBlank()) ? null : Arrays.asList(locations.split(","));
        eruptNodeMicroservice.removeInstance(nodeName, accessToken, locationList);
    }

}
