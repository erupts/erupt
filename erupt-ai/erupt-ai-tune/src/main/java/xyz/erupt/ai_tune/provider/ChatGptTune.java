package xyz.erupt.ai_tune.provider;

import org.springframework.stereotype.Component;

/**
 * OpenAI, plus any generic OpenAI-compatible endpoint registered under the
 * {@code OpenAIAdapter} provider (LLaMA-Factory API, vLLM-backed trainers, proxies...).
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Component
public class ChatGptTune extends OpenAITune {

    @Override
    public String[] codes() {
        return new String[]{"ChatGpt", "OpenAIAdapter"};
    }

}
