package xyz.erupt.ai.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.ai.model.McpServer;
import xyz.erupt.ai.service.McpServerService;
import xyz.erupt.annotation.fun.OperationHandler;

import java.util.List;

/**
 * @author YuePeng
 */
@Service
public class RefreshMcpServerHandler implements OperationHandler<McpServer, Void> {

    @Resource
    private McpServerService mcpServerService;

    @Override
    public String exec(List<McpServer> data, Void v, String[] param) {
        data.forEach(mcpServerService::register);
        return null;
    }

}
