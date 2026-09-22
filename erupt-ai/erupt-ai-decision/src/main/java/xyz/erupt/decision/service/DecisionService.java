package xyz.erupt.decision.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
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

    /**
     * The model everything falls back to: the enabled row that carries the flag, and nothing else.
     * Standing in for it with whichever row came back first would let a judgement be answered by a
     * model nobody chose, with nothing on screen to say so.
     */
    public DecisionModel defaultModel() {
        DecisionModel model = eruptDao.lambdaQuery(DecisionModel.class)
                .eq(DecisionModel::getDefaultModel, true).eq(DecisionModel::getEnable, true).limit(1).one();
        if (null == model) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("decision.no_default_model"));
        }
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

    /** Asks a decision declared in the admin */
    public Decision run(String code, Object state, DecisionModel override) {
        DecisionDef def = this.def(code);
        return this.evaluate(this.config(def, override), state, questions(def));
    }

    /**
     * Which model answers a stored decision: an explicit one wins, then the decision's own, then
     * the default. A locked model stops answering however it was picked — the alternative is to
     * quietly promote a different one, which is the thing a decision must never do.
     */
    public DecisionModel config(DecisionDef def, DecisionModel override) {
        if (null != override) return override;
        DecisionModel own = def.getDecisionModel();
        if (null == own) return this.defaultModel();
        if (!Boolean.TRUE.equals(own.getEnable())) {
            throw new EruptWebApiRuntimeException(
                    I18nTranslate.$translate("decision.model_locked") + " " + own.getName());
        }
        return own;
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
        JsonObject asked = new JsonObject();
        questions(def).forEach((id, question) -> asked.add(id, question.toJson()));
        JsonObject request = new JsonObject();
        request.add("state", state);
        request.add("questions", asked);
        return this.raw(this.config(def, override), request);
    }

    private static Map<String, Question<?>> questions(DecisionDef def) {
        Map<String, Question<?>> questions = new LinkedHashMap<>();
        def.getQuestions().stream()
                .sorted(Comparator.comparing(DecisionQuestion::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(it -> questions.put(it.getCode(), it.toQuestion()));
        return questions;
    }

}
