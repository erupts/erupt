package xyz.erupt.cloud.server.node;

import cn.hutool.http.HttpUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.server.config.EruptCloudServerProp;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * node 节点定时任务
 *
 * @author YuePeng
 * date 2022/2/3 21:36
 */
@AllArgsConstructor
@Component
@Slf4j
public class NodeWorker implements Runnable {

    private final NodeManager nodeManager;

    private final EruptCloudServerProp eruptCloudServerProp;

    @PostConstruct
    public void postConstruct() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this, 0,
                eruptCloudServerProp.getNodeSurviveCheckTime(), TimeUnit.MILLISECONDS);
    }

    @SneakyThrows
    @Override
    public void run() {
        for (MetaNode node : nodeManager.findAllNodes()) {
            if (node.getLocations().removeIf(location -> !health(node.getNodeName(), location, 2))) {
                nodeManager.putNode(node);
            }
        }
    }

    private boolean health(String nodeName, String location, int retryNum) {
        if (retryNum <= 0) {
            log.error("remove node: {} -> {}", nodeName, location);
            return false;
        }
        try {
            if (HttpUtil.createGet(location + CloudRestApiConst.NODE_HEALTH).timeout(1000).execute().isOk()) {
                return true;
            } else {
                return health(nodeName, location, retryNum - 1);
            }
        } catch (Exception e) {
            log.error("remove node: {} -> {}", nodeName, location);
            return false;
        }
    }

}
