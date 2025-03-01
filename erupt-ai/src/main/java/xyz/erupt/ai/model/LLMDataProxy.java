package xyz.erupt.ai.model;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;

/**
 * @author YuePeng
 * date 2025/3/1 18:19
 */
@Component
public class LLMDataProxy implements DataProxy<LLM> {

    @Override
    public void beforeAdd(LLM llm) {
        llm.getConfig();
    }
}
