package xyz.erupt.cloud.server.node;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import xyz.erupt.cloud.server.config.EruptCloudServerProp;
import xyz.erupt.cloud.server.model.CloudNode;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2022/1/29
 */
@Component
public class NodeManager {

    public static final String NODE_SPACE = "node:";

    @Resource
    private EruptDao eruptDao;

    private RedisTemplate<String, MetaNode> redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate<?, ?> redisTemplate) {
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        this.redisTemplate = (RedisTemplate<String, MetaNode>) redisTemplate;
    }

    @Resource
    private EruptCloudServerProp eruptCloudServerProp;

    // Round-robin cursor shared by every caller that forwards to a node.
    private final AtomicInteger counter = new AtomicInteger();

    private String geneKey(String nodeName) {
        return eruptCloudServerProp.getCloudNameSpace() + NodeManager.NODE_SPACE + nodeName;
    }

    public MetaNode getNode(String nodeName) {
        return redisTemplate.opsForValue().get(geneKey(nodeName));
    }

    public void putNode(MetaNode metaNode) {
        if (metaNode.getLocations().isEmpty()) {
            this.removeNode(metaNode.getNodeName());
        } else {
            redisTemplate.opsForValue().set(geneKey(metaNode.getNodeName()), metaNode,
                    eruptCloudServerProp.getNodeExpireTime(), TimeUnit.MILLISECONDS);
        }
    }

    public void removeNode(String nodeName) {
        redisTemplate.delete(geneKey(nodeName));
    }

    //Remove the specified instance (null-safe; no-op when the node or location is already gone)
    public void removeNodeInstance(String nodeName, String instanceAddress) {
        MetaNode metaNode = getNode(nodeName);
        if (null == metaNode) {
            return;
        }
        if (metaNode.getLocations().remove(instanceAddress)) {
            this.putNode(metaNode);
        }
    }

    /**
     * Round-robin ordered instance locations: the first element is the primary pick, the rest are
     * failover candidates in rotation order. A single shared cursor keeps the distribution even
     * across concurrent callers (interceptor + internal router).
     */
    public List<String> pickLocations(MetaNode metaNode) {
        List<String> locations = new ArrayList<>(metaNode.getLocations());
        if (locations.isEmpty()) {
            throw new EruptWebApiRuntimeException(metaNode.getNodeName() + " has no available instance");
        }
        if (locations.size() == 1) {
            return locations;
        }
        int start = Math.abs(counter.getAndIncrement() % locations.size());
        List<String> ordered = new ArrayList<>(locations.size());
        for (int i = 0; i < locations.size(); i++) {
            ordered.add(locations.get((start + i) % locations.size()));
        }
        return ordered;
    }

    /**
     * Best-effort eviction of a dead instance discovered on the request path, so subsequent
     * requests stop hitting it instead of waiting for the {@link NodeWorker} survival check.
     */
    public void evictInstance(String nodeName, String instanceAddress) {
        try {
            removeNodeInstance(nodeName, instanceAddress);
        } catch (Exception ignore) {
            // eviction is opportunistic; the survival check will reconcile eventually
        }
    }


    public List<MetaNode> findAllNodes() {
        List<String> keys = eruptDao.lambdaQuery(CloudNode.class).list().stream().map(it ->
                eruptCloudServerProp.getCloudNameSpace() + NODE_SPACE + it.getNodeName()
        ).collect(Collectors.toList());
        if (!keys.isEmpty()) {
            List<MetaNode> metaNodes = Optional.ofNullable(redisTemplate.opsForValue().multiGet(keys)).orElse(new ArrayList<>());
            metaNodes.removeIf(Objects::isNull);
            return metaNodes;
        } else {
            return new ArrayList<>();
        }
    }

}
