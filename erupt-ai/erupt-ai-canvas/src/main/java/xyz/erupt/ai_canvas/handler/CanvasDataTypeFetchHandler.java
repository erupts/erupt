package xyz.erupt.ai_canvas.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_canvas.service.AiCanvasService;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.List;

/**
 * Data source types available for a canvas, one per registered
 * {@code CanvasModelProvider}.
 *
 * @author YuePeng
 * date 2026/8/30
 */
@Component
public class CanvasDataTypeFetchHandler implements ChoiceFetchHandler<Void> {

    @Resource
    private AiCanvasService aiCanvasService;

    @Override
    public List<VLModel> fetch(String[] params) {
        return aiCanvasService.getProviders().keySet().stream().sorted()
                .map(it -> new VLModel(it, it)).toList();
    }

}
