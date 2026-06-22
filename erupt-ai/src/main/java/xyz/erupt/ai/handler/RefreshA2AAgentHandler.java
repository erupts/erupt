package xyz.erupt.ai.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.ai.model.A2AAgent;
import xyz.erupt.ai.service.A2AAgentService;
import xyz.erupt.annotation.fun.OperationHandler;

import java.util.List;

/**
 * @author YuePeng
 */
@Service
public class RefreshA2AAgentHandler implements OperationHandler<A2AAgent, Void> {

    @Resource
    private A2AAgentService a2aAgentService;

    @Override
    public String exec(List<A2AAgent> data, Void v, String[] param) {
        data.forEach(a2aAgentService::refresh);
        return null;
    }

}
