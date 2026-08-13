package xyz.erupt.core.service;

import com.google.gson.JsonObject;
import org.springframework.http.HttpMethod;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQuery;

import java.util.List;
import java.util.Map;

/**
 * Routing SPI for erupt models that physically live on a remote erupt-cloud node.
 * <p>
 * The erupt data pipeline above {@link xyz.erupt.core.invoke.DataProcessorManager}
 * (validation, deserialization, DataProxy hooks) is bound to a local Java class, which
 * a remote node erupt does not have on the server side. So routing happens one layer up:
 * whenever a model is remote the semantic operation is forwarded verbatim to the node,
 * which re-runs its own full pipeline. Transport is HTTP per call; nothing is cached
 * except the schema, which is fetched lazily.
 * <p>
 * Implemented by erupt-cloud-server and registered via {@link xyz.erupt.core.invoke.EruptRemoteRouterManager}.
 * When no implementation is present every call site behaves exactly as before.
 *
 * @author YuePeng
 */
public interface EruptRemoteRouter {

    // Whether the given dotted name ("nodeName.eruptName") resolves to a live remote node
    boolean isRemote(String eruptName);

    // Lightweight placeholder model (no schema fetch) so callers can resolve a remote erupt by name
    EruptModel resolveErupt(String eruptName);

    // Remote model carrying the node's schema (build) metadata, lazily fetched for display
    EruptModel resolveEruptView(String eruptName);

    // Fully-qualified names of every erupt exposed by currently live nodes
    List<String> remoteEruptNames();

    /**
     * Generic passthrough to the owning node's erupt-api — one method for the long tail
     * (tree / reference / checkbox / choice / init-value ...). {@code pathTemplate} is the
     * node-side path with {@code {erupt}} as a placeholder for the simple erupt name, e.g.
     * {@code "/erupt-api/data/tree/{erupt}"} or {@code "/erupt-api/data/{erupt}/reference-table/dept"}
     * (use {@link xyz.erupt.core.constant.EruptRestPath#ERUPT_NAME_HOLDER} for the placeholder).
     * The node runs its own controllers; the raw response body is returned unparsed.
     */
    String proxy(String eruptName, HttpMethod httpMethod, String pathTemplate, Object body);

    Page tableQuery(String eruptName, TableQuery tableQuery);

    Map<String, Object> findById(String eruptName, String id);

    // Returns the primary key when the node exposes it, otherwise null
    Object insert(String eruptName, JsonObject data);

    void update(String eruptName, JsonObject data);

    void delete(String eruptName, List<Object> ids);

}
