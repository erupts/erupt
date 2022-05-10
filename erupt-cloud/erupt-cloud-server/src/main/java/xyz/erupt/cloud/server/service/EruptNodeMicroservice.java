package xyz.erupt.cloud.server.service;

import org.springframework.stereotype.Service;
import xyz.erupt.cloud.server.base.NodeRegisterType;
import xyz.erupt.cloud.server.model.CloudNode;
import xyz.erupt.cloud.server.node.MetaNode;
import xyz.erupt.cloud.server.node.NodeManager;
import xyz.erupt.cloud.server.node.NodeWorker;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.util.IpUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
    private HttpServletRequest request;

    @Resource
    private NodeManager nodeManager;

    @Resource
    private NodeWorker nodeWorker;

    public CloudNode findNodeByAppName(String nodeName, String accessToken) {
        CloudNode cloudNode = eruptDao.queryEntity(CloudNode.class, CloudNode.NODE_NAME + " = :" + CloudNode.NODE_NAME, new HashMap<String, Object>() {{
            this.put(CloudNode.NODE_NAME, nodeName);
        }});
        if (null == cloudNode) {
            throw new RuntimeException(nodeName + " not found");
        }
        if (!cloudNode.getAccessToken().equals(accessToken)) {
            throw new RuntimeException(cloudNode.getNodeName() + " Access token invalid");
        }
        return cloudNode;
    }

    //生成节点地址
    private String geneNodeLocation(MetaNode metaNode) {
        return request.getScheme() + "://" + IpUtil.getIpAddr(request) + ":" + metaNode.getPort() +
                (metaNode.getContextPath() == null ? "" : metaNode.getContextPath());
    }

    public void registerNode(CloudNode cloudNode, MetaNode metaNode) {
        if (NodeRegisterType.MANUAL.equals(cloudNode.getRegisterType())) {
            metaNode.setManualLocations(Arrays.asList(cloudNode.getAddresses().split("\\|")));
        }
        Optional.ofNullable(nodeManager.getNode(metaNode.getNodeName())).ifPresent(it -> metaNode.getLocations().addAll(it.getLocations()));
        metaNode.getLocations().add(geneNodeLocation(metaNode));
        metaNode.getErupts().forEach(it -> metaNode.getEruptMap().put(it, it));
        metaNode.setRegisterTime(new Date());
        nodeManager.putNode(metaNode);
    }

    public void removeInstance(String nodeName, String accessToken) {
        this.findNodeByAppName(nodeName, accessToken);
        nodeWorker.run();
    }

}