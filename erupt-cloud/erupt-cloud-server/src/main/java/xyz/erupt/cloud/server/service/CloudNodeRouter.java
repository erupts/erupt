package xyz.erupt.cloud.server.service;

import xyz.erupt.cloud.common.http.CloudHttp;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.cloud.server.config.EruptCloudServerProp;
import org.springframework.web.client.RestClient;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.web.util.UriUtils;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.cloud.common.node.CloudProtocol;
import xyz.erupt.cloud.server.util.NodeBuilds;
import xyz.erupt.cloud.server.util.NodeRequestNormalizer;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.util.RemoteRequestValidator;
import xyz.erupt.core.view.R;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Function;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.erupt.cloud.common.consts.CloudCommonConst;
import xyz.erupt.cloud.server.node.MetaNode;
import xyz.erupt.cloud.server.node.NodeManager;
import xyz.erupt.cloud.server.util.CloudServerUtil;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.EruptRemoteRouterManager;
import xyz.erupt.core.service.EruptRemoteRouter;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQuery;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    private RestClient nodeRestClient;

    @Resource
    private EruptCloudServerProp eruptCloudServerProp;

    @Resource
    private I18nTranslate i18nTranslate;

    @Resource
    private NodePowerService nodePowerService;


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
    public EruptModel resolveEruptModel(String eruptName) {
        return EruptModel.remote(eruptName, nodeName(eruptName), resolveBuild(eruptName));
    }

    // Field metadata of a node's erupt, briefly cached: request validation, reference labels, Excel and the
    // endpoints the server answers for a node itself all read it. Independent of the user, not of the language:
    // a node translates titles and option labels, so each language has its own entry
    private static final long BUILD_TTL_MILLIS = 60_000;

    private final Map<String, Map.Entry<Long, EruptBuildModel>> builds = new ConcurrentHashMap<>();

    public EruptBuildModel resolveBuild(String eruptName) {
        String key = eruptName + EruptConst.DOT + i18nTranslate.currentLang();
        Map.Entry<Long, EruptBuildModel> cached = builds.get(key);
        if (null != cached && cached.getKey() > System.currentTimeMillis()) return cached.getValue();
        EruptBuildModel build = GsonFactory.getGson().fromJson(resolveEruptView(eruptName).getEruptJson(), EruptBuildModel.class);
        // a v2 node declares only what it sets: completed, and refused when it does not read back (PROTOCOL.md §12.3)
        if (metaNode(nodeName(eruptName)).thin()) NodeBuilds.complete(build, CloudProtocol.V2);
        builds.put(key, Map.entry(System.currentTimeMillis() + BUILD_TTL_MILLIS, build));
        return build;
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
        return proxy(eruptName, httpMethod, pathTemplate, body, Collections.emptySet());
    }

    /**
     * @param enforced the checks the server vouches for (signed as Erupt-Enforced), e.g. POWER when it reads a
     *                 referenced erupt the user reaches only through the erupt holding the reference
     */
    public String proxy(String eruptName, HttpMethod httpMethod, String pathTemplate, Object body, Set<CloudSignature.Enforced> enforced) {
        String path = pathTemplate.replace(EruptRestPath.ERUPT_NAME_HOLDER, "/" + simpleName(eruptName));
        return exchange(eruptName, httpMethod, path, body, enforced);
    }

    // The in-process callers (AI tools, workflow) reach a node here without the interceptor. A v1 node checks them
    // itself; a v2 node checks nothing, so the server enforces the rights and the record first (PROTOCOL.md §12.5)

    @Override
    public Page tableQuery(String eruptName, TableQuery tableQuery) {
        Set<CloudSignature.Enforced> enforced = Collections.emptySet();
        if (thin(eruptName)) {
            enforce(eruptName, EruptRouter.Power.QUERY);
            String problem = RemoteRequestValidator.validateQuery(resolveBuild(eruptName), GsonFactory.getGson().toJsonTree(tableQuery).getAsJsonObject());
            if (null != problem) throw new EruptApiErrorTip(R.error(problem));
            enforced = QUERIED;
        }
        return GsonFactory.getGson().fromJson(
                proxy(eruptName, HttpMethod.POST, EruptRestPath.ERUPT_DATA + "/table" + EruptRestPath.ERUPT_NAME_HOLDER, tableQuery, enforced), Page.class);
    }

    @Override
    public Map<String, Object> findById(String eruptName, String id) {
        Set<CloudSignature.Enforced> enforced = Collections.emptySet();
        if (thin(eruptName)) {
            enforce(eruptName, EruptRouter.Power.EDIT, EruptRouter.Power.VIEW_DETAILS);
            enforced = VOUCHED;
        }
        return GsonFactory.getGson().fromJson(proxy(eruptName, HttpMethod.GET, EruptRestPath.ERUPT_DATA + EruptRestPath.ERUPT_NAME_HOLDER
                + "/" + UriUtils.encodePathSegment(id, StandardCharsets.UTF_8), null, enforced), MAP_TYPE);
    }

    @Override
    public Object insert(String eruptName, JsonObject data) {
        Set<CloudSignature.Enforced> enforced = Collections.emptySet();
        if (thin(eruptName)) {
            enforce(eruptName, EruptRouter.Power.ADD);
            checkRecord(eruptName, data);
            enforced = RECORDED;
        }
        // The node's add endpoint returns R without the generated primary key
        proxy(eruptName, HttpMethod.POST, EruptRestPath.ERUPT_DATA_MODIFY + EruptRestPath.ERUPT_NAME_HOLDER, data, enforced);
        return null;
    }

    @Override
    public void update(String eruptName, JsonObject data) {
        Set<CloudSignature.Enforced> enforced = Collections.emptySet();
        JsonObject record = data;
        if (thin(eruptName)) {
            enforce(eruptName, EruptRouter.Power.EDIT);
            // a caller may send only the values it changes (a cell): the stored row, patched, is what gets checked and saved
            String pk = AnnotationProcess.jsonToAnnotation(Erupt.class, resolveBuild(eruptName).getEruptModel().getEruptJson()).primaryKeyCol();
            JsonElement id = data.get(pk);
            if (null == id || !id.isJsonPrimitive()) throw new EruptApiErrorTip(R.error(I18nTranslate.$translate("erupt.incorrect_format")));
            JsonElement stored = JsonParser.parseString(proxy(eruptName, HttpMethod.GET, EruptRestPath.ERUPT_DATA + EruptRestPath.ERUPT_NAME_HOLDER
                    + "/" + UriUtils.encodePathSegment(id.getAsString(), StandardCharsets.UTF_8), null, VOUCHED));
            record = stored.isJsonObject() ? stored.getAsJsonObject() : new JsonObject();
            for (Map.Entry<String, JsonElement> entry : data.entrySet()) record.add(entry.getKey(), entry.getValue());
            checkRecord(eruptName, record);
            enforced = RECORDED;
        }
        proxy(eruptName, HttpMethod.POST, EruptRestPath.ERUPT_DATA_MODIFY + EruptRestPath.ERUPT_NAME_HOLDER + "/update", record, enforced);
    }

    @Override
    public void delete(String eruptName, List<Object> ids) {
        Set<CloudSignature.Enforced> enforced = Collections.emptySet();
        if (thin(eruptName)) {
            enforce(eruptName, EruptRouter.Power.DELETE);
            enforced = VOUCHED;
        }
        proxy(eruptName, HttpMethod.POST, EruptRestPath.ERUPT_DATA_MODIFY + EruptRestPath.ERUPT_NAME_HOLDER + "/delete", ids, enforced);
    }

    private static final Set<CloudSignature.Enforced> VOUCHED = EnumSet.of(CloudSignature.Enforced.POWER);

    private static final Set<CloudSignature.Enforced> QUERIED = EnumSet.of(CloudSignature.Enforced.POWER, CloudSignature.Enforced.QUERY);

    private static final Set<CloudSignature.Enforced> RECORDED = EnumSet.of(CloudSignature.Enforced.POWER, CloudSignature.Enforced.RECORD);

    private boolean thin(String eruptName) {
        return metaNode(nodeName(eruptName)).thin();
    }

    // The user's rights and the node's declared ones, at least one of the required rights in both
    private void enforce(String eruptName, EruptRouter.Power... required) {
        if (!NodePowerService.allows(nodePowerService.userPower(eruptName), required)
                || !NodePowerService.allows(Optional.ofNullable(resolveBuild(eruptName).getPower()).orElseGet(PowerObject::new), required)) {
            throw new EruptNoLegalPowerException();
        }
    }

    private void checkRecord(String eruptName, JsonObject record) {
        R<?> problem = RemoteRequestValidator.validateRecord(resolveBuild(eruptName), record);
        if (null != problem) throw new EruptApiErrorTip(problem);
    }

    private MetaNode metaNode(String nodeName) {
        MetaNode metaNode = nodeManager.getNode(nodeName);
        if (null == metaNode) throw new EruptWebApiRuntimeException("'" + nodeName + "' node not ready");
        return metaNode;
    }

    /**
     * A body the server checked, as a v2 node receives it (PROTOCOL.md §12.4): a query or a record normalized against
     * the erupt's build, anything else as it is.
     */
    public Object normalize(String eruptName, Object body, Set<CloudSignature.Enforced> enforced) {
        return normalize(eruptName, null, body, enforced);
    }

    /**
     * @param build the model the body is a query or record of, null for the erupt's own (a sub-table row names its tab's)
     */
    public Object normalize(String eruptName, EruptBuildModel build, Object body, Set<CloudSignature.Enforced> enforced) {
        if (null == body) return null;
        boolean query = enforced.contains(CloudSignature.Enforced.QUERY);
        if (!query && !enforced.contains(CloudSignature.Enforced.RECORD)) return body;
        JsonElement json = GsonFactory.getGson().toJsonTree(body);
        if (!json.isJsonObject()) return body;
        if (null == build) build = resolveBuild(eruptName);
        String prefix = nodeName(eruptName) + EruptConst.DOT;
        Function<String, EruptBuildModel> builds = target -> resolveBuild(prefix + target);
        return query ? NodeRequestNormalizer.query(build, json.getAsJsonObject(), builds)
                : NodeRequestNormalizer.record(build, json.getAsJsonObject(), builds);
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
    private String exchange(String eruptName, HttpMethod method, String path, Object body, Set<CloudSignature.Enforced> enforced) {
        String nodeName = nodeName(eruptName);
        MetaNode metaNode = metaNode(nodeName);
        boolean thin = metaNode.thin();
        // a v2 node takes values the way §12.4 writes them, and only signed calls
        if (thin) body = normalize(eruptName, body, enforced);
        String payload = null == body ? null : GsonFactory.getGson().toJson(body);
        List<String> locations = nodeManager.pickLocations(metaNode);
        Exception lastError = null;
        for (int i = 0; i < locations.size(); i++) {
            String location = locations.get(i);
            try {
                String user = CloudHttp.base64(GsonFactory.getGson().toJson(MetaContext.getUser()));
                long timestamp = System.currentTimeMillis() / 1000;
                String enforcedHeader = enforced.isEmpty() ? null : CloudSignature.Enforced.header(enforced);
                RestClient.RequestBodySpec httpRequest = nodeRestClient.method(method).uri(location + path)
                        .headers(CloudHttp.header(CloudCommonConst.HEADER_ACCESS_TOKEN,
                                eruptCloudServerProp.isPlainAccessToken() && !thin ? metaNode.getAccessToken() : null))
                        .headers(CloudHttp.header(EruptMutualConst.TOKEN, MetaContext.getToken()))
                        // the user's language, as a forwarded request carries it: the node translates its answer
                        .headers(CloudHttp.header(EruptMutualConst.LANG, i18nTranslate.getLang()))
                        .header(EruptMutualConst.ERUPT, simpleName(eruptName))
                        .header(EruptMutualConst.USER, user)
                        .header(CloudSignature.HEADER_TIMESTAMP, Long.toString(timestamp))
                        .headers(CloudHttp.header(CloudSignature.HEADER_ENFORCED, enforcedHeader))
                        .header(CloudSignature.HEADER_SIGNATURE, CloudSignature.sign(metaNode.getAccessToken(), method.name(), path,
                                timestamp, enforcedHeader, user, null == payload ? new byte[0] : payload.getBytes(StandardCharsets.UTF_8)));
                if (null != payload) {
                    httpRequest.contentType(MediaType.APPLICATION_JSON).body(payload);
                }
                ResponseEntity<String> httpResponse = CloudHttp.exchange(httpRequest);
                String responseBody = httpResponse.getBody();
                if (httpResponse.getStatusCode().value() != HttpStatus.OK.value()) {
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
