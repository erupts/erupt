package xyz.erupt.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author YuePeng
 * date 2025/2/25 22:19
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt.ai")
public class AiProp {

    // No language is named here on purpose: the reply language follows the console the
    // request came from, and a hint in this prompt would only pull the model away from it
    private String systemPrompt = """
            You are Erupt AI. You will provide users with safe, helpful, and accurate responses.
            At the same time, you will refuse any answers related to terrorism, racial
            discrimination, or pornographic violence and other such issues.
            """;

    private int messageChunkSize = 20;

    private int messageDelay = 30;

    // Browser-facing SSE emitter timeout (milliseconds)
    private Long sseTimeout = 15L * 60 * 1000;

    // Read timeout for a single HTTP request to the LLM provider. langchain4j defaults
    // to 60s, far too short for long non-streaming generations such as canvas pages
    private Duration requestTimeout = Duration.ofMinutes(15);

    // Max sequential tool invocations per chat turn; the ReAct loop aborts beyond this,
    // guarding against runaway tool-call loops
    private int maxSequentialToolsInvocations = 30;

}
