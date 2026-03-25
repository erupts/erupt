package xyz.erupt.ai.model;

import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.Collection;
import java.util.Map;

/**
 * @author YuePeng
 * date 2026/3/24 22:53
 */
public class McpServerDataProxy implements DataProxy<McpServer> {

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        for (Map<String, Object> map : list) {
            map.put(LambdaSee.field(McpServer::getTools), "xxxx");
        }
    }

}
