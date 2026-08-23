package xyz.erupt.cloud.server.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.cloud.server.config.EruptCloudServerProp;
import xyz.erupt.cloud.server.model.CloudNode;
import xyz.erupt.cloud.server.node.MetaNode;
import xyz.erupt.cloud.server.node.NodeManager;
import xyz.erupt.cloud.server.node.NodeWorker;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2021/12/20 23:54
 */
@Service
public class EruptNodeMicroservice {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private NodeManager nodeManager;

    @Resource
    private NodeWorker nodeWorker;

    @Resource
    private EruptCloudServerProp eruptCloudServerProp;

    public CloudNode findNodeByAppName(String nodeName, String accessToken) {
        CloudNode cloudNode = eruptDao.lambdaQuery(CloudNode.class).eq(CloudNode::getNodeName, nodeName).one();
        if (null == cloudNode) {
            throw new RuntimeException("NodeName: '" + nodeName + "' not found");
        }
        if (eruptCloudServerProp.isValidateAccessToken()) {
            if (!cloudNode.getAccessToken().equals(accessToken)) {
                throw new RuntimeException(cloudNode.getNodeName() + " Access token invalid");
            }
        }
        return cloudNode;
    }

    public void registerNode(CloudNode cloudNode, MetaNode metaNode) {
        Optional.ofNullable(nodeManager.getNode(metaNode.getNodeName())).ifPresent(it -> metaNode.getLocations().addAll(it.getLocations()));
        metaNode.getLocations().addAll(Arrays.asList(metaNode.getNodeAddress()));
        metaNode.setRegisterTime(new Date());
        nodeManager.putNode(metaNode);
    }

    public void safeRemoveInstance(String nodeName, String accessToken) {
        this.findNodeByAppName(nodeName, accessToken);
        nodeWorker.run(); // Poll the availability of all IP instances under the nodeName
    }

    /**
     * Precisely drop the instance locations a node reports while gracefully shutting down, so the
     * server stops routing to it immediately instead of waiting for the survival check.
     */
    public void removeInstance(String nodeName, String accessToken, List<String> locations) {
        this.findNodeByAppName(nodeName, accessToken);
        if (null == locations || locations.isEmpty()) {
            nodeWorker.run();
            return;
        }
        for (String location : locations) {
            nodeManager.removeNodeInstance(nodeName, location);
        }
    }

}