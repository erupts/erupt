package xyz.erupt.decision.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

import java.util.HashSet;
import java.util.Set;

/**
 * A decision is called by code long after it is saved, so everything that can be checked now
 * is checked now — a bad rubric must not surface as a provider error in production.
 *
 * @author YuePeng
 */
@Component
public class DecisionDefDataProxy implements DataProxy<DecisionDef> {

    @Override
    public void beforeAdd(DecisionDef def) {
        this.verify(def);
    }

    @Override
    public void beforeUpdate(DecisionDef def) {
        this.verify(def);
    }

    /** The row is edited through the component its type calls for, so the json is spread back out */
    @Override
    public void editBehavior(DecisionDef def) {
        if (null != def.getQuestions()) def.getQuestions().forEach(DecisionQuestion::unpack);
    }

    private void verify(DecisionDef def) {
        if (null == def.getQuestions() || def.getQuestions().isEmpty()) {
            throw new EruptWebApiRuntimeException("A decision needs at least one question");
        }
        Set<String> codes = new HashSet<>();
        for (DecisionQuestion question : def.getQuestions()) {
            question.pack();
            if (StringUtils.isBlank(question.getCode())) {
                throw new EruptWebApiRuntimeException("Every question needs a code");
            }
            if (!codes.add(question.getCode())) {
                throw new EruptWebApiRuntimeException("Duplicate question code: " + question.getCode());
            }
            try {
                question.toQuestion();
            } catch (Exception e) {
                throw new EruptWebApiRuntimeException("Question '" + question.getCode() + "': " + e.getMessage());
            }
        }
    }

}
