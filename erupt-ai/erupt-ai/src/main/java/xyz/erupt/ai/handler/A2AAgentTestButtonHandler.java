package xyz.erupt.ai.handler;

import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.ai.model.A2AAgent;
import xyz.erupt.ai.service.A2AAgentService;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;

import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2026-07-19
 */
@Service
public class A2AAgentTestButtonHandler implements EruptButtonHandler<A2AAgent> {

    @Resource
    private A2AAgentService a2aAgentService;

    @Override
    public String click(A2AAgent agent, String[] params) {
        if (null == agent.getAgentUrl() || agent.getAgentUrl().isBlank()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("Agent URL") + " " + I18nTranslate.$translate("erupt.notnull"));
        }
        try {
            AgentCard card = a2aAgentService.fetchAgentCard(agent);
            String skills = null == card.skills() ? "" :
                    card.skills().stream().map(AgentSkill::name).collect(Collectors.joining("\n"));
            return "alert(" + GsonFactory.getGson().toJson(card.name() + "\n\nSkills:\n" + skills) + ")";
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

}
