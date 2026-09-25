package xyz.erupt.decision.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.decision.Decision;
import xyz.erupt.decision.core.DecisionCore;
import xyz.erupt.decision.model.DecisionDef;
import xyz.erupt.decision.model.DecisionModel;
import xyz.erupt.decision.model.DecisionQuestion;
import xyz.erupt.decision.question.Question;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resolves what to ask and who to ask, and keeps the provider key on this side of the wire.
 *
 * @author YuePeng
 */
@Service
public class DecisionService {

    @Resource
    private EruptDao eruptDao;

    /** The model everything falls back to; any enabled one will do when none is marked default */
    public DecisionModel defaultModel() {
        DecisionModel model = eruptDao.lambdaQuery(DecisionModel.class)
                .eq(DecisionModel::getDefaultModel, true).eq(DecisionModel::getEnable, true).limit(1).one();
        if (null == model) {
            model = eruptDao.lambdaQuery(DecisionModel.class).eq(DecisionModel::getEnable, true).limit(1).one();
        }
        if (null == model) throw new EruptWebApiRuntimeException("No decision model is configured");
        return model;
    }

    public DecisionModel model(String name) {
        DecisionModel model = eruptDao.lambdaQuery(DecisionModel.class)
                .eq(DecisionModel::getName, name).eq(DecisionModel::getEnable, true).limit(1).one();
        if (null == model) throw new EruptWebApiRuntimeException("No such decision model: " + name);
        return model;
    }

    public DecisionDef def(String code) {
        DecisionDef def = eruptDao.lambdaQuery(DecisionDef.class)
                .eq(DecisionDef::getCode, code).eq(DecisionDef::getEnable, true).limit(1).one();
        if (null == def) throw new EruptWebApiRuntimeException("No such decision: " + code);
        return def;
    }

    public DecisionCore core(DecisionModel config) {
        DecisionCore core = DecisionCore.get(config.getProvider());
        if (null == core) throw new EruptWebApiRuntimeException("Unknown decision provider: " + config.getProvider());
        return core;
    }

    public Decision evaluate(DecisionModel config, Object state, Map<String, Question<?>> questions) {
        return this.core(config).evaluate(config, state, questions);
    }

    public Decision run(String code, Object state) {
        return this.run(code, state, null);
    }

    /** Asks a decision declared in the admin; an explicit model wins, then its own, then the default */
    public Decision run(String code, Object state, DecisionModel override) {
        DecisionDef def = this.def(code);
        DecisionModel config = null != override ? override
                : null == def.getDecisionModel() ? this.defaultModel() : def.getDecisionModel();
        return this.evaluate(config, state, questions(def));
    }

    public JsonObject raw(DecisionModel config, JsonObject request) {
        return this.core(config).raw(config, request);
    }

    /**
     * Runs a stored decision and hands back the provider's own answer body, so an HTTP caller
     * gets the documented System One shape and never has to hold a provider key.
     */
    public JsonObject rawRun(String code, JsonElement state, DecisionModel override) {
        DecisionDef def = this.def(code);
        DecisionModel config = null != override ? override
                : null == def.getDecisionModel() ? this.defaultModel() : def.getDecisionModel();
        JsonObject asked = new JsonObject();
        questions(def).forEach((id, question) -> asked.add(id, question.toJson()));
        JsonObject request = new JsonObject();
        request.add("state", state);
        request.add("questions", asked);
        return this.raw(config, request);
    }

    private static Map<String, Question<?>> questions(DecisionDef def) {
        Map<String, Question<?>> questions = new LinkedHashMap<>();
        def.getQuestions().stream()
                .sorted(Comparator.comparing(DecisionQuestion::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(it -> questions.put(it.getCode(), it.toQuestion()));
        return questions;
    }

}
