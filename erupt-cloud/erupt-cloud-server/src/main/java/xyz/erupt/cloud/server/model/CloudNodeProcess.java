package xyz.erupt.cloud.server.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.TagsFetchHandler;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.cloud.server.node.MetaNode;
import xyz.erupt.cloud.server.node.NodeManager;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.tpl.engine.EngineConst;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author YuePeng
 * date 2024/5/7 22:02
 */
@Component
@Slf4j
public class CloudNodeProcess implements DataProxy<CloudNode>, TagsFetchHandler, Tpl.TplHandler {

    @Resource
    private NodeManager nodeManager;

    @Resource
    private EruptDao eruptDao;

    @Override
    public void afterUpdate(CloudNode cloudNode) {
        DataProxy.super.afterUpdate(cloudNode);
    }

    @Override
    public void beforeAdd(CloudNode cloudNode) {
        if (null == cloudNode.getAccessToken()) cloudNode.setAccessToken(Erupts.generateCode(16).toUpperCase());
    }

    @Override
    public void beforeUpdate(CloudNode cloudNode) {
        this.beforeAdd(cloudNode);
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        String nodeNameField = LambdaSee.field(CloudNode::getNodeName);
        for (Map<String, Object> map : list) {
            Optional.ofNullable(map.get(LambdaSee.field(CloudNode::getAccessToken))).ifPresent(it -> {
                String token = it.toString();
                map.put(LambdaSee.field(CloudNode::getAccessToken), token.substring(0, 3) + "******" + token.substring(token.length() - 3));
            });
            map.put(LambdaSee.field(CloudNode::getEruptNum), '-');
            map.put(LambdaSee.field(CloudNode::getInstanceNum), '-');
            map.put(LambdaSee.field(CloudNode::getVersion), '-');
            map.put(LambdaSee.field(CloudNode::getEruptModuleNum), '-');
            try {
                MetaNode metaNode = nodeManager.getNode(map.get(nodeNameField).toString());
                Optional.ofNullable(nodeManager.getNode(map.get(nodeNameField).toString())).ifPresent(metaNode1 -> {
                    Function<Collection<String>, Object> function = (it) -> null == it ? 0 : String.format("<a href='javascript:alert(`%s`);'>%d</a>", String.join("\\u000a", it), it.size());
                    map.put(LambdaSee.field(CloudNode::getEruptNum), function.apply(metaNode.getErupts()));
                    map.put(LambdaSee.field(CloudNode::getInstanceNum), metaNode.getLocations().size());
                    map.put(LambdaSee.field(CloudNode::getEruptModuleNum), function.apply(metaNode.getEruptModules()));
                    map.put(LambdaSee.field(CloudNode::getVersion), metaNode.getVersion());
                });
            } catch (Exception e) {
                map.put(LambdaSee.field(CloudNode::getVersion), String.format("<span style='color:#f00'>%s</span>", e.getMessage()));
                log.warn("node warn → " + map.get(nodeNameField), e);
            }
        }
    }

    @Override
    public void afterDelete(CloudNode cloudNode) {
        nodeManager.removeNode(cloudNode.getNodeName());
    }

    @Override
    public List<String> fetchTags(String[] params) {
        return eruptDao.getJdbcTemplate().queryForList("select name from e_upms_user", String.class);
    }

    @Override
    public void bindTplData(Map<String, Object> binding, String[] params) {
        CloudNode cloudNode = (CloudNode) binding.get(EngineConst.INJECT_ROW);
        MetaNode metaNode = nodeManager.getNode(cloudNode.getNodeName());
        if (null == metaNode) {
            binding.put("instances", "[]");
        } else {
            binding.put("instances", GsonFactory.getGson().toJson(metaNode.getLocations()));
        }
    }

}
