package xyz.erupt.ai_tune.provider;

import org.springframework.stereotype.Component;

/**
 * Zhipu GLM fine-tuning: the OpenAI protocol under {@code /api/paas/v4} with the legacy
 * top-level {@code hyperparameters} object.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@Component
public class GLMTune extends OpenAITune {

    @Override
    public String[] codes() {
        return new String[]{"GLM"};
    }

    @Override
    protected String apiPoint() {
        return "/api/paas/v4";
    }

    @Override
    protected boolean methodBlock() {
        return false;
    }

}
