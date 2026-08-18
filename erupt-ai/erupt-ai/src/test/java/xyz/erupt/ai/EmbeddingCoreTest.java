package xyz.erupt.ai;

import org.junit.jupiter.api.Test;
import xyz.erupt.ai.core.EmbeddingCore;
import xyz.erupt.ai.embedding.*;
import xyz.erupt.ai.model.EmbeddingLLM;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author YuePeng
 * date 2026/8/17
 */
public class EmbeddingCoreTest {

    // Instantiating self-registers into the EmbeddingCore registry
    private static final List<EmbeddingCore> PROVIDERS = List.of(
            new OpenAICompatibleEmbedding(), new GeminiEmbedding(), new OllamaEmbedding(),
            new QwenEmbedding(), new GLMEmbedding(), new DoubaoEmbedding(), new SiliconFlowEmbedding(),
            new JinaEmbedding(), new VoyageEmbedding(), new MistralEmbedding(), new CohereEmbedding()
    );

    @Test
    public void embeddingRegistry() {
        for (EmbeddingCore provider : PROVIDERS) {
            assertSame(provider, EmbeddingCore.get(provider.code()));
            // Every provider must offer complete form hints
            assertNotNull(provider.model());
            assertNotNull(provider.api());
            assertNotNull(provider.dimension());
            assertTrue(provider.dimension() > 0);
        }
    }

    @Test
    public void buildWithoutNetwork() {
        for (EmbeddingCore provider : PROVIDERS) {
            EmbeddingLLM config = new EmbeddingLLM();
            config.setProvider(provider.code());
            config.setModel(provider.model());
            config.setApiUrl(provider.api());
            config.setApiKey("test-placeholder");
            config.setDimension(provider.dimension());
            // Building must not hit the network
            assertNotNull(provider.build(config));
        }
    }

}
