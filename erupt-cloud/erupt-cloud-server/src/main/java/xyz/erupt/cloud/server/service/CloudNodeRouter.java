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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.cloud.server.config.EruptCloudServerProp;
import xyz.erupt.cloud.server.node.MetaNode;
import xyz.erupt.cloud.server.node.NodeManager;
import xyz.erupt.cloud.server.util.CloudServerUtil;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.EruptRemoteRouterManager;
import xyz.erupt.core.service.EruptRemoteRouter;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQuery;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Routes in-process erupt operations (AI/MCP tools, workflow, internal callers) against remote
 * node erupts. The browser path is still short-circuited by {@code EruptCloudServerInterceptor};
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
        return new EruptModel(eruptName, nodeName(eruptName));
    }

    @Override
    public EruptModel resolveEruptView(String eruptName) {
        EruptModel eruptModel = resolveErupt(eruptName);
        String body = proxy(eruptName, HttpMethod.GET, EruptRestPath.ERUPT_BUILD + EruptRestPath.ERUPT_NAME_HOLDER, null);
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
    public String proxy(String eruptName, HttpMethod httpMethod, String pathTemplate, Object body) {
        String path = pathTemplate.replace(EruptRestPath.ERUPT_NAME_HOLDER, "/" + simpleName(eruptName));
        return exchange(eruptName, Method.valueOf(httpMethod.name()), path, body);
    }

    @Override
    public Page tableQuery(String eruptName, TableQuery tableQuery) {
        return GsonFactory.getGson().fromJson(
                proxy(eruptName, HttpMethod.POST, EruptRestPath.ERUPT_DATA + "/table" + EruptRestPath.ERUPT_NAME_HOLDER, tableQuery), Page.class);
    }

    @Override
    public Map<String, Object> findById(String eruptName, String id) {
        return GsonFactory.getGson().fromJson(
                proxy(eruptName, HttpMethod.GET, EruptRestPath.ERUPT_DATA + EruptRestPath.ERUPT_NAME_HOLDER + "/" + id, null), MAP_TYPE);
    }

    @Override
    public Object insert(String eruptName, JsonObject data) {
        // The node's add endpoint returns R without the generated primary key
        proxy(eruptName, HttpMethod.POST, EruptRestPath.ERUPT_DATA_MODIFY + EruptRestPath.ERUPT_NAME_HOLDER, data);
        return null;
    }

    @Override
    public void update(String eruptName, JsonObject data) {
        proxy(eruptName, HttpMethod.POST, EruptRestPath.ERUPT_DATA_MODIFY + EruptRestPath.ERUPT_NAME_HOLDER + "/update", data);
    }

    @Override
    public void delete(String eruptName, List<Object> ids) {
        proxy(eruptName, HttpMethod.POST, EruptRestPath.ERUPT_DATA_MODIFY + EruptRestPath.ERUPT_NAME_HOLDER + "/delete", ids);
    }

    private String nodeName(String eruptName) {
        return eruptName.substring(0, eruptName.lastIndexOf(EruptConst.DOT));
    }

    private String simpleName(String eruptName) {
        return eruptName.substring(eruptName.lastIndexOf(EruptConst.DOT) + 1);
    }

    /**
     * Forward one operation to the owning node.
     *
     * @param eruptName full dotted name ("nodeName.eruptName")
     * @param path      node erupt-api path (relative to the node location)
     * @param body      request body to serialize as JSON, or null
     */
    private String exchange(String eruptName, Method method, String path, Object body) {
        String nodeName = nodeName(eruptName);
        MetaNode metaNode = nodeManager.getNode(nodeName);
        if (null == metaNode) {
            throw new EruptWebApiRuntimeException("'" + nodeName + "' node not ready");
        }
        String payload = null == body ? null : GsonFactory.getGson().toJson(body);
        List<String> locations = nodeManager.pickLocations(metaNode);
        Exception lastError = null;
        for (int i = 0; i < locations.size(); i++) {
            String location = locations.get(i);
            HttpRequest httpRequest = HttpUtil.createRequest(method, location + path)
                    .header(CloudCommonConst.HEADER_ACCESS_TOKEN, metaNode.getAccessToken())
                    .header(EruptMutualConst.TOKEN, MetaContext.getToken())
                    .header(EruptMutualConst.ERUPT, simpleName(eruptName))
                    .header(EruptMutualConst.USER, Base64Encoder.encode(GsonFactory.getGson().toJson(MetaContext.getUser())))
                    .timeout(eruptCloudServerProp.getNodeRequestTimeout());
            if (null != payload) {
                httpRequest.body(payload);
            }
            try (HttpResponse httpResponse = httpRequest.execute()) {
                String responseBody = httpResponse.body();
                if (httpResponse.getStatus() != HttpStatus.OK.value()) {
                    throw new EruptWebApiRuntimeException(nodeName + " -> " + responseBody);
                }
                return responseBody;
            } catch (EruptWebApiRuntimeException e) {
                throw e; // application-level error from the node, never fail over
            } catch (Exception e) {
                lastError = e;
                // Only fail over when the connection never reached the node.
                if (i + 1 < locations.size() && CloudServerUtil.isConnectFailure(e)) {
                    log.warn("node {} instance {} unreachable, failing over: {}", nodeName, location, e.getMessage());
                    nodeManager.evictInstance(nodeName, location);
                    continue;
                }
                throw new EruptWebApiRuntimeException(location + " -> " + e.getMessage());
            }
        }
        throw new EruptWebApiRuntimeException(nodeName + " -> " + (null == lastError ? "no available instance" : lastError.getMessage()));
    }

}
