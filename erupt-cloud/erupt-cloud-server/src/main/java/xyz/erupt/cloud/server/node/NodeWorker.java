package xyz.erupt.cloud.server.node;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.cloud.server.config.EruptCloudServerProp;
import xyz.erupt.cloud.server.util.CloudServerUtil;

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
            if (node.getLocations().removeIf(location ->
                    !CloudServerUtil.retryableNodeHealth(node.getNodeName(),
                            location, 2, 200))) {
                nodeManager.putNode(node);
            }
        }
    }

}
