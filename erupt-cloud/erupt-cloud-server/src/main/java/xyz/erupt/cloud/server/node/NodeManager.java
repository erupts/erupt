package xyz.erupt.cloud.server.node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import xyz.erupt.cloud.server.config.EruptCloudServerProp;
import xyz.erupt.cloud.server.model.CloudNode;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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

    //移除指定实例
    public void removeNodeInstance(String nodeName, String instanceAddress) {
        MetaNode metaNode = getNode(nodeName);
        metaNode.getLocations().removeIf(it -> it.equals(instanceAddress));
        this.putNode(metaNode);
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
