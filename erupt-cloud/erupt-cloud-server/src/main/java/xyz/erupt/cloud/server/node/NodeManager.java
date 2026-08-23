package xyz.erupt.cloud.server.node;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import xyz.erupt.cloud.server.config.EruptCloudServerProp;
import xyz.erupt.cloud.server.model.CloudNode;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.jpa.dao.EruptDao;

import java.nio.charset.StandardCharsets;
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
@Slf4j
@Component
public class NodeManager {

    public static final String NODE_SPACE = "node:";

    @Resource
    private EruptDao eruptDao;

    private RedisTemplate<String, MetaNode> redisTemplate;

    /**
     * Dedicated template over the shared connection factory. Values are stored as JSON instead of
     * JDK serialization, so adding fields to {@link xyz.erupt.cloud.common.model.NodeInfo} never
     * invalidates cached entries again; a legacy (pre-JSON) binary entry fails to parse and is
     * treated as absent — the next node heartbeat rewrites it.
     */
    @Autowired
    public void setRedisTemplate(RedisTemplate<?, ?> redisTemplate) {
        RedisTemplate<String, MetaNode> template = new RedisTemplate<>();
        template.setConnectionFactory(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new RedisSerializer<MetaNode>() {
            @Override
            public byte[] serialize(MetaNode metaNode) {
                return null == metaNode ? null : GsonFactory.getGson().toJson(metaNode).getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public MetaNode deserialize(byte[] bytes) {
                if (null == bytes || bytes.length == 0) return null;
                try {
                    return GsonFactory.getGson().fromJson(new String(bytes, StandardCharsets.UTF_8), MetaNode.class);
                } catch (Exception e) {
                    log.warn("Dropped an incompatible node cache entry (pre-JSON format), it will be rebuilt on the next heartbeat");
                    return null;
                }
            }
        });
        template.afterPropertiesSet();
        this.redisTemplate = template;
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
