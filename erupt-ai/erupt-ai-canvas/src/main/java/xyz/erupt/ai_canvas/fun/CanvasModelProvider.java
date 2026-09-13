package xyz.erupt.ai_canvas.fun;

import xyz.erupt.ai_canvas.model.AiCanvasModel;
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

    // Teaches the LLM how pages create / update / delete rows of this source
    // (SDK functions, payload shapes, form rules); Markdown. Injected only when a
    // binding of this type allows at least one write; null means the source is
    // read-only and the allow* switches of its bindings are ignored
    default String writeGuide() {
        return null;
    }

    // Object with langchain4j @Tool methods the LLM calls during generation to
    // verify its planned queries actually work (ReAct); null disables verification
    /**
     * ReAct verification tool for the LLM round, or {@code null} when the source cannot verify.
     *
     * @param bindings the canvas bindings of this data source type; write dry runs must honour their switches
     */
    default Object verifyTool(List<AiCanvasModel> bindings) {
        return null;
    }

}
