package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.base.BaseLLMConfig;

/**
 * @author YuePeng
 * date 2025/2/26 22:58
 */
@Component
public class QWen extends CommonLLM {
    @Override
    public String code() {
        return "通义千问";
    }

    @Override
    public String model() {
        return "qwen-plus";
    }

    @Override
    public BaseLLMConfig config() {
        return new BaseLLMConfig("https://dashscope.aliyuncs.com", "qwen-plus", "");
    }
}
