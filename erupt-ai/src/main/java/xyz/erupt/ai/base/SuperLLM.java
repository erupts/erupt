package xyz.erupt.ai.base;

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

public abstract class SuperLLM {

    private static final Map<String, SuperLLM> llms= new HashMap<>();

    public SuperLLM() {
        llms.put(this.code(), this);
    }

    public static SuperLLM getLLM(String code) {
        return llms.get(code);
    }

    public abstract String code();

    public abstract String model();

    public abstract BaseLLMConfig config();

    public abstract ChatCompletionResponse chat(LLM llm, String userPrompt, List<ChatCompletionMessage> assistantPrompt);

    public abstract void chatSse(LLM llm, String userPrompt, List<ChatCompletionMessage> assistantPrompt, Consumer<SseListener> listener);

    public static class H implements ChoiceFetchHandler {
        @Override
        public List<VLModel> fetch(String[] params) {
            return llms.keySet().stream().map(it -> new VLModel(it, it)).collect(Collectors.toList());
        }
    }

}
