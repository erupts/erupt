package xyz.erupt.cloud.server.node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import xyz.erupt.cloud.server.config.EruptCloudServerProp;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author YuePeng
 * date 2022/1/29
 */
@Component
public class NodeManager {

    public static final String NODE_SPACE = "node:";

    private RedisTemplate<String, MetaNode> redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate<?, ?> redisTemplate) {
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        this.redisTemplate = (RedisTemplate<String, MetaNode>) redisTemplate;
    }

    @Resource
    private EruptCloudServerProp eruptCloudServerProp;

    private String geneKey(String nodeName) {
        return eruptCloudServerProp.getCloudNameSpace() + NodeManager.NODE_SPACE + nodeName;
    }

    public MetaNode getNode(String nodeName) {
        return redisTemplate.opsForValue().get(geneKey(nodeName));
    }

    public void putNode(MetaNode metaNode) {
        if (metaNode.getLocations().size() <= 0) {
            this.removeNode(metaNode.getNodeName());
        } else {
            redisTemplate.opsForValue().set(geneKey(metaNode.getNodeName()), metaNode,
                    eruptCloudServerProp.getNodeExpireTime(), TimeUnit.MILLISECONDS);
        }
    }

    public void removeNode(String nodeName) {
        redisTemplate.delete(geneKey(nodeName));
    }


    public List<MetaNode> findAllNodes() {
        List<MetaNode> metaNodes = new ArrayList<>();
        Optional.ofNullable(redisTemplate.keys(eruptCloudServerProp.getCloudNameSpace() + NODE_SPACE + "*")).flatMap(keys ->
                Optional.ofNullable(redisTemplate.opsForValue().multiGet(keys))).ifPresent(metaNodes::addAll
        );
        return metaNodes;
    }

}
