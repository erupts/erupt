package xyz.erupt.ai.core;

import xyz.erupt.ai.model.LLM;
import xyz.erupt.ai.pojo.ChatCompletionMessage;
import xyz.erupt.ai.pojo.ChatCompletionResponse;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class LlmCore {

    private static final Map<String, LlmCore> llms = new HashMap<>();

    public LlmCore() {
        llms.put(this.code(), this);
    }

    public static LlmCore getLLM(String code) {
        return llms.get(code);
    }

    public static LlmCore getLLM(LLM llm) {
        return llms.get(llm.getLlm());
    }

    public abstract String code();

    public abstract String model();

    public abstract String api();

    public abstract LlmConfig config();

    public abstract ChatCompletionResponse chat(LlmRequest llmRequest, String userPrompt, List<ChatCompletionMessage> assistantPrompt);

    public abstract void chatSse(LlmRequest llmRequest, String userPrompt, List<ChatCompletionMessage> assistantPrompt, Consumer<SseListener> listener);

    public static class H implements ChoiceFetchHandler {
        @Override
        public List<VLModel> fetch(String[] params) {
            return llms.keySet().stream().map(it -> new VLModel(it, it)).collect(Collectors.toList());
        }
    }

}
