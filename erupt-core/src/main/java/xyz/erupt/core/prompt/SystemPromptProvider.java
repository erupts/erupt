package xyz.erupt.core.prompt;

import java.util.ArrayList;
import java.util.List;

public interface SystemPromptProvider {

    static List<SystemPromptProvider> getRegisteredProviders() {
        return ProviderRegistry.providers;
    }

    static void registerProvider(SystemPromptProvider provider) {
        ProviderRegistry.providers.add(provider);
    }

    String getPrompt();

    class ProviderRegistry {
        private static final List<SystemPromptProvider> providers = new ArrayList<>();
    }

}
