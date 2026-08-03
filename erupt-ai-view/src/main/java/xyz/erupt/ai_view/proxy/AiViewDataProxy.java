package xyz.erupt.ai_view.proxy;

import org.springframework.stereotype.Component;
import xyz.erupt.ai_view.controller.AiViewController;
import xyz.erupt.ai_view.model.AiView;
import xyz.erupt.annotation.fun.DataProxy;

import java.util.Collection;
import java.util.Map;

/**
 * @author YuePeng
 * date 2026/8/3
 */
@Component
public class AiViewDataProxy implements DataProxy<AiView> {

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        list.forEach(row -> row.put("path", AiViewController.RENDER_PATH + "/" + row.get("id")));
    }

}
