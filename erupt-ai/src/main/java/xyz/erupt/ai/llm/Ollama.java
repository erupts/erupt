package xyz.erupt.ai.llm;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.*;
import xyz.erupt.ai.pojo.ChatCompletionMessage;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author YuePeng
 * date 2025/2/26 22:58
 */
@Component
public class Ollama extends LlmCore {

    @Override
    public String code() {
        return Ollama.class.getSimpleName();
    }

    @Override
    public String model() {
        return "nomic-embed-text";
    }

    @Override
    public String api() {
        return "http://localhost:11434";
    }

    @Override
    public String chat(LlmRequest llmRequest, String userMessage, List<ChatCompletionMessage> assistantPrompt) {
        OllamaChatModel model = OllamaChatModel.builder()
                .modelName(llmRequest.getModel())
                .topP(llmRequest.getTop_p())
                .temperature(llmRequest.getTemperature())
                .build();
        return model.chat(userMessage);
    }

    @Override
    public void chatSse(LlmRequest llmRequest, String userMessage, List<ChatCompletionMessage> assistantPrompt, Consumer<SseListener> listener) {
        StreamingChatModel model = OllamaStreamingChatModel.builder()
                .modelName(llmRequest.getModel())
                .topP(llmRequest.getTop_p())
                .temperature(llmRequest.getTemperature())
                .build();
        this.streamerReact(listener, model, userMessage, assistantPrompt);
    }

}
