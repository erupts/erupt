package xyz.erupt.ai.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.a2a.client.Client;
import io.a2a.spec.AgentCard;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.service.A2AAgentService;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.core.prompt.SystemPromptProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Expose registered A2A agents as callable sub-agent tools.
 *
 * @author YuePeng
 * date 2026/5/15
 */
@AiToolbox
@Component
public class A2AAgentTools implements SystemPromptProvider {

    @Resource
    private A2AAgentService a2aAgentService;

    @PostConstruct
    public void init() {
        SystemPromptProvider.registerProvider(this);
    }

    @Override
    public String getPrompt() {
        return """
                ## A2A Sub-Agents
                Call `listA2AAgents` to discover available sub-agents and their skills.
                When a user request matches a sub-agent's capability, delegate it via `invokeA2AAgent`.
                Pass the user's intent as a clear, self-contained message — include all context the sub-agent needs.
                Present the sub-agent's response directly to the user without unnecessary wrapping.
                """;
    }

    @Tool("List all registered A2A sub-agents with their name, description, and skills. " +
            "Call this before invokeA2AAgent to discover which agents are available.")
    public String listA2AAgents() {
        Map<Long, AgentCard> cards = A2AAgentService.getAGENT_CARDS();
        if (cards.isEmpty()) return "No A2A agents registered.";
        List<String> lines = new ArrayList<>();
        for (AgentCard card : cards.values()) {
            StringBuilder sb = new StringBuilder("name: ").append(card.name());
            if (card.description() != null) {
                sb.append("\ndescription: ").append(card.description());
            }
            if (card.skills() != null && !card.skills().isEmpty()) {
                sb.append("\nskills: ").append(card.skills().stream()
                        .map(s -> s.name() + (s.description() != null ? " — " + s.description() : ""))
                        .reduce((a, b) -> a + ", " + b).orElse(""));
            }
            lines.add(sb.toString());
        }
        return String.join("\n\n", lines);
    }

    @Tool("Delegate a task to a registered A2A sub-agent by its exact name. " +
            "Use listA2AAgents first to discover available agents and their capabilities.")
    public String invokeA2AAgent(
            @P("Exact agent name as returned by listA2AAgents") String agentName,
            @P("The task or message to send to the sub-agent") String message) {
        for (Map.Entry<Long, AgentCard> entry : A2AAgentService.getAGENT_CARDS().entrySet()) {
            if (entry.getValue().name().equals(agentName)) {
                Client client = A2AAgentService.getAGENT_CLIENTS().get(entry.getKey());
                if (client == null) {
                    return "Agent '" + agentName + "' is registered but not connected.";
                }
                try {
                    return a2aAgentService.call(client, message);
                } catch (Exception e) {
                    return "Error calling agent '" + agentName + "': " + e.getMessage();
                }
            }
        }
        return "Agent not found: " + agentName + ". Call listA2AAgents to see available agents.";
    }
}
