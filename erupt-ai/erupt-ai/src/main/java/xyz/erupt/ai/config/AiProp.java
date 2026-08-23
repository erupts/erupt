package xyz.erupt.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2025/2/25 22:19
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt.ai")
public class AiProp {

    private String systemPrompt = """
            You are Erupt AI, and you are better at having conversations in English and Chinese.
            You will provide users with safe, helpful, and accurate responses. At the same time,
            you will refuse any answers related to terrorism, racial discrimination,
            or pornographic violence and other such issues.
            """;

    private int messageChunkSize = 20;

    private int messageDelay = 30;

    private Long sseTimeout = 15L * 60 * 1000;

    // Max sequential tool invocations per chat turn; the ReAct loop aborts beyond this,
    // guarding against runaway tool-call loops
    private int maxSequentialToolsInvocations = 30;

}
