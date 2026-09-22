package xyz.erupt.decision.core;

import com.google.gson.JsonObject;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.decision.Decision;
import xyz.erupt.decision.answer.Answer;
import xyz.erupt.decision.answer.Usage;
import xyz.erupt.decision.model.DecisionModel;
import xyz.erupt.decision.question.Question;

import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A System One provider. The protocol — state plus typed questions in, typed answers out — is
 * the same for every one of them, so a provider only has to say where it lives and how to reach
 * it; implementations register themselves on construction, as the rest of erupt's plug points do.
 *
 * @author YuePeng
 */
public abstract class DecisionCore {

    private static final Map<String, DecisionCore> cores = new HashMap<>();

    public DecisionCore() {
        cores.put(this.code(), this);
    }

    public static DecisionCore get(String code) {
        return cores.get(code);
    }

    public abstract String code();

    /** Endpoint this provider ships with, offered when the provider is picked */
    public abstract String api();

    /** Model name this provider ships with */
    public abstract String model();

    /** Transport only: post one System One request and hand back its body */
    protected abstract JsonObject call(DecisionModel config, JsonObject request);

    /** Asks typed questions and reads the answers back through the questions that asked them */
    public Decision evaluate(DecisionModel config, Object state, Map<String, Question<?>> questions) {
        if (questions.isEmpty()) throw new EruptWebApiRuntimeException("A decision needs at least one question");
        JsonObject asked = new JsonObject();
        questions.forEach((id, question) -> asked.add(id, question.toJson()));
        JsonObject request = new JsonObject();
        request.add("state", GsonFactory.getGson().toJsonTree(state));
        request.add("questions", asked);
        JsonObject response = this.raw(config, request);
        JsonObject answers = response.getAsJsonObject("answers");
        Map<String, Answer> parsed = new LinkedHashMap<>();
        Map<Question<?>, String> ids = new IdentityHashMap<>();
        questions.forEach((id, question) -> {
            JsonObject answer = answers.getAsJsonObject(id);
            if (null == answer) throw new EruptWebApiRuntimeException("The provider answered nothing for: " + id);
            parsed.put(id, question.parse(answer));
            ids.put(question, id);
        });
        return new Decision(response.get("model").getAsString(), usage(response), parsed, ids);
    }

    /** Passthrough for a caller that speaks the wire format already; the model comes from config */
    public JsonObject raw(DecisionModel config, JsonObject request) {
        if (!request.has("model") || request.get("model").isJsonNull()) {
            request.addProperty("model", config.getModel());
        }
        return this.call(config, request);
    }

    private static Usage usage(JsonObject response) {
        JsonObject usage = response.getAsJsonObject("usage");
        return null == usage ? new Usage(0, 0)
                : new Usage(usage.get("input_tokens").getAsInt(), usage.get("output_tokens").getAsInt());
    }

    public static class H implements ChoiceFetchHandler<Void> {

        @Override
        public List<VLModel> fetch(String[] params) {
            return cores.keySet().stream().map(it -> new VLModel(it, it))
                    .sorted(Comparator.comparing(VLModel::getLabel)).toList();
        }
    }

}
