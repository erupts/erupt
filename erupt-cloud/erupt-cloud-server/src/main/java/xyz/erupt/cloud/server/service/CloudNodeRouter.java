package xyz.erupt.cloud.server.service;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.cloud.server.config.EruptCloudServerProp;
import xyz.erupt.cloud.server.node.MetaNode;
import xyz.erupt.cloud.server.node.NodeManager;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.EruptRemoteRouterManager;
import xyz.erupt.core.service.EruptRemoteRouter;
import xyz.erupt.core.view.EruptModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Routes in-process erupt operations (AI/MCP tools, internal callers) against remote node
 * erupts. The browser path is still short-circuited by {@code EruptCloudServerInterceptor};
 * this covers every caller that funnels through the erupt data pipeline instead of raw HTTP.
 * <p>
 * Each operation is forwarded to the node's standard erupt-api using the same header/auth
 * scheme as the interceptor, so the node re-runs its own validation, permission and DataProxy
 * pipeline. Only the schema is fetched (lazily); data is never cached.
 *
 * @author YuePeng
 */
@Slf4j
@Service
public class CloudNodeRouter implements EruptRemoteRouter {

    @Resource
    private NodeManager nodeManager;

    @Resource
    private EruptCloudServerProp eruptCloudServerProp;

    private final AtomicInteger counter = new AtomicInteger();

    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    @PostConstruct
    public void init() {
        EruptRemoteRouterManager.register(this);
    }

    @Override
    public boolean isRemote(String eruptName) {
        int dot = eruptName.lastIndexOf(EruptConst.DOT);
        return dot > 0 && null != nodeManager.getNode(eruptName.substring(0, dot));
    }

    @Override
    public EruptModel resolveErupt(String eruptName) {
        return new EruptModel(eruptName, eruptName.substring(0, eruptName.lastIndexOf(EruptConst.DOT)));
    }

    @Override
    public EruptModel resolveEruptView(String eruptName) {
        EruptModel eruptModel = resolveErupt(eruptName);
        String body = exchange(eruptName, Method.GET, EruptRestPath.ERUPT_BUILD, null, null);
        eruptModel.setEruptJson(GsonFactory.getGson().fromJson(body, JsonObject.class));
        return eruptModel;
    }

    @Override
    public List<String> remoteEruptNames() {
        List<String> names = new ArrayList<>();
        for (MetaNode node : nodeManager.findAllNodes()) {
            for (String erupt : node.getErupts()) {
                names.add(node.getNodeName() + EruptConst.DOT + erupt);
            }
        }
        return names;
    }

    @Override
    public Map<String, Object> findById(String eruptName, String id) {
        String body = exchange(eruptName, Method.GET, EruptRestPath.ERUPT_DATA, "/" + id, null);
        return GsonFactory.getGson().fromJson(body, MAP_TYPE);
    }

    @Override
    public Object insert(String eruptName, JsonObject data) {
        // The node's add endpoint returns R without the generated primary key
        exchange(eruptName, Method.POST, EruptRestPath.ERUPT_DATA_MODIFY, null, data);
        return null;
    }

    @Override
    public void update(String eruptName, JsonObject data) {
        exchange(eruptName, Method.POST, EruptRestPath.ERUPT_DATA_MODIFY, "/update", data);
    }

    @Override
    public void delete(String eruptName, List<Object> ids) {
        exchange(eruptName, Method.POST, EruptRestPath.ERUPT_DATA_MODIFY, "/delete", ids);
    }

    /**
     * Forward one operation to the owning node.
     *
     * @param eruptName full dotted name ("nodeName.eruptName")
     * @param basePath  node erupt-api base path (build / data / data-modify)
     * @param subPath   trailing path segment appended after the erupt name, or null
     * @param body      request body to serialize as JSON, or null
     */
    private String exchange(String eruptName, Method method, String basePath, String subPath, Object body) {
        int dot = eruptName.lastIndexOf(EruptConst.DOT);
        String nodeName = eruptName.substring(0, dot);
        String simpleName = eruptName.substring(dot + 1);
        MetaNode metaNode = nodeManager.getNode(nodeName);
        if (null == metaNode) {
            throw new EruptWebApiRuntimeException("'" + nodeName + "' node not ready");
        }
        String url = pickLocation(metaNode) + basePath + "/" + simpleName + (null == subPath ? "" : subPath);
        HttpRequest httpRequest = HttpUtil.createRequest(method, url)
                .header(CloudCommonConst.HEADER_ACCESS_TOKEN, metaNode.getAccessToken())
                .header(EruptMutualConst.TOKEN, MetaContext.getToken())
                .header(EruptMutualConst.ERUPT, simpleName)
                .header(EruptMutualConst.USER, Base64Encoder.encode(GsonFactory.getGson().toJson(MetaContext.getUser())))
                .timeout(eruptCloudServerProp.getNodeRequestTimeout());
        if (null != body) {
            httpRequest.body(GsonFactory.getGson().toJson(body));
        }
        try (HttpResponse httpResponse = httpRequest.execute()) {
            String responseBody = httpResponse.body();
            if (httpResponse.getStatus() != HttpStatus.OK.value()) {
                throw new EruptWebApiRuntimeException(nodeName + " -> " + responseBody);
            }
            return responseBody;
        }
    }

    private String pickLocation(MetaNode metaNode) {
        String[] locations = metaNode.getLocations().toArray(new String[0]);
        if (locations.length == 0) {
            throw new EruptWebApiRuntimeException(metaNode.getNodeName() + " has no available instance");
        }
        return locations[locations.length == 1 ? 0 : Math.abs(counter.getAndIncrement() % locations.length)];
    }

}
