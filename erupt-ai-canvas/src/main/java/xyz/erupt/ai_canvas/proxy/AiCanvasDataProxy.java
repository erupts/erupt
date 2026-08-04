package xyz.erupt.ai_canvas.proxy;

import org.springframework.stereotype.Component;
import xyz.erupt.ai_canvas.controller.AiCanvasController;
import xyz.erupt.ai_canvas.model.AiCanvas;
import xyz.erupt.annotation.fun.DataProxy;

import java.util.Collection;
import java.util.Map;

/**
 * @author YuePeng
 * date 2026/8/3
 */
@Component
public class AiCanvasDataProxy implements DataProxy<AiCanvas> {

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        list.forEach(row -> row.put("path", AiCanvasController.RENDER_PATH + "/" + row.get("id")));
    }

}
