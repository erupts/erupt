package xyz.erupt.ai.llm;

import org.springframework.stereotype.Component;
import xyz.erupt.ai.core.OpenAI;

/**
 * @author YuePeng
 * date 2025/2/22 16:37
 */
@Component
public class Mimo extends OpenAI {

    @Override
    public String model() {
        return "MiMo-V2.5-Pro";
    }

    @Override
    public String api() {
        return "https://token-plan-cn.xiaomimimo.com";
    }

}
