package xyz.erupt.cloud.server.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import xyz.erupt.cloud.common.consts.CloudRestApiConst;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.view.R;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Set;

/**
 * The code a node runs (PROTOCOL.md §5.5): a field's fetched options, onchange, auto-complete and button, and a
 * row operation with its form values, all reach a node with {@code NodeCapability.INVOKE} as one call and an R out.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Service
public class NodeInvokeService {

    private static final Set<CloudSignature.Enforced> VOUCHED = EnumSet.of(CloudSignature.Enforced.POWER);

    private static final Type ANSWER = new TypeToken<R<JsonElement>>() {
    }.getType();

    @Resource
    private CloudNodeRouter cloudNodeRouter;

    // a field's code takes {form, value}; a row operation's takes the browser's own body, under the operation code
    public enum Kind {
        OPTIONS, ONCHANGE, AUTOCOMPLETE, BUTTON, OPERATION, OPERATION_FORM;

        public String wire() {
            return name().toLowerCase().replace('_', '-');
        }
    }

    /**
     * @param form  the form values the browser sent, or null
     * @param value what the user typed, for {@link Kind#AUTOCOMPLETE}
     * @return the node's R
     */
    public R<JsonElement> invoke(String erupt, Kind kind, String field, JsonElement form, String value) {
        JsonObject body = new JsonObject();
        body.add("form", null == form || !form.isJsonObject() ? new JsonObject() : form);
        if (null != value) body.addProperty("value", value);
        return invoke(erupt, kind, field, body);
    }

    /**
     * @param name the field, or the operation code
     * @param body what the node is handed as it is
     * @return the node's R
     */
    public R<JsonElement> invoke(String erupt, Kind kind, String name, JsonElement body) {
        String answer = cloudNodeRouter.proxy(erupt, HttpMethod.POST, CloudRestApiConst.NODE_INVOKE + EruptRestPath.ERUPT_NAME_HOLDER
                + "/" + kind.wire() + "/" + UriUtils.encodePathSegment(name, StandardCharsets.UTF_8), body, VOUCHED);
        R<JsonElement> r = GsonFactory.getGson().fromJson(answer, ANSWER);
        return null == r ? R.error(erupt + ": no answer") : r;
    }

    /**
     * What a successful answer carries, for the endpoints that answer a bare value; a refusal is the node's message.
     */
    public JsonElement data(R<JsonElement> r) {
        if (!r.isSuccess()) throw new EruptApiErrorTip(r.getMessage(), R.PromptWay.MESSAGE);
        return r.getData();
    }

}
