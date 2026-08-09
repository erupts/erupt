package xyz.erupt.ai_canvas.fun;

import xyz.erupt.annotation.fun.VLModel;

import java.util.List;

/**
 * A selectable data source family for the view designer. Implement as a Spring
 * bean and it is picked up automatically; erupt models are built in.
 *
 * @author YuePeng
 * date 2026/8/4
 */
public interface CanvasModelProvider {

    // Data source type code shown in the designer, e.g. 'erupt'
    String type();

    // Selectable models of this source
    List<VLModel> models();

    // Structure description of one model, injected into the generation prompt
    String describe(String model);

    // Teaches the LLM how pages query this source (SDK functions, shapes); Markdown
    String queryGuide();

    // Object with langchain4j @Tool methods the LLM calls during generation to
    // verify its planned queries actually work (ReAct); null disables verification
    default Object verifyTool() {
        return null;
    }

}
