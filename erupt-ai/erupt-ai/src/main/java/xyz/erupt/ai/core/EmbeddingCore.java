package xyz.erupt.ai.core;

import xyz.erupt.ai.model.EmbeddingLLM;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Embedding provider registry, mirrors {@link LlmCore}.
 * Subclasses self-register on construction (instantiated as Spring beans).
 *
 * @author YuePeng
 * date 2026/8/17
 */
public abstract class EmbeddingCore {

    private static final Map<String, EmbeddingCore> registry = new HashMap<>();

    public EmbeddingCore() {
        registry.put(this.code(), this);
    }

    public static EmbeddingCore get(String code) {
        return registry.get(code);
    }

    public abstract String code();

    // Default model name hint shown when the provider is picked in the edit form
    public abstract String model();

    // Default api url hint shown when the provider is picked in the edit form
    public abstract String api();

    // Default output dimension hint shown when the provider is picked in the edit form
    public abstract Integer dimension();

    public abstract dev.langchain4j.model.embedding.EmbeddingModel build(EmbeddingLLM config);

    public static class H implements ChoiceFetchHandler<Void> {
        @Override
        public List<VLModel> fetch(String[] params) {
            return registry.keySet().stream().map(it -> new VLModel(it, it))
                    .sorted(Comparator.comparing(VLModel::getLabel)).collect(Collectors.toList());
        }
    }

}
