package xyz.erupt.ai_canvas.handler;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_canvas.model.AiCanvas;
import xyz.erupt.ai_canvas.service.AiCanvasService;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.List;

/**
 * Target models of the chosen data source type, linked to the dataType field.
 *
 * @author YuePeng
 * date 2026/8/30
 */
@Component
public class CanvasTargetModelFetchHandler implements ChoiceFetchHandler<AiCanvas> {

    @Resource
    private AiCanvasService aiCanvasService;

    // Contract: the superset of every type's models, so stored values always resolve to labels
    @Override
    public List<VLModel> fetch(String[] params) {
        return aiCanvasService.getProviders().values().stream()
                .flatMap(it -> it.models().stream()).toList();
    }

    @Override
    public List<VLModel> fetchFilter(AiCanvas canvas, String[] params) {
        if (null == canvas || StringUtils.isBlank(canvas.getDataType())) return this.fetch(params);
        return aiCanvasService.provider(canvas.getDataType()).models();
    }

}
