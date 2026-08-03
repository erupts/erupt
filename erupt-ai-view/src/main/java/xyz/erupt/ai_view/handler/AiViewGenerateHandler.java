package xyz.erupt.ai_view.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_view.model.AiView;
import xyz.erupt.ai_view.service.AiViewService;
import xyz.erupt.annotation.fun.OperationHandler;

import java.util.List;

/**
 * @author YuePeng
 * date 2026/8/3
 */
@Component
public class AiViewGenerateHandler implements OperationHandler<AiView, Void> {

    @Resource
    private AiViewService aiViewService;

    @Override
    public String exec(List<AiView> data, Void unused, String[] param) {
        aiViewService.generate(data.get(0));
        return null;
    }

}
