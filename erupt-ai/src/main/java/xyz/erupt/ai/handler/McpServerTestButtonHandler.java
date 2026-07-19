package xyz.erupt.ai.handler;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.McpClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.ai.model.McpServer;
import xyz.erupt.ai.service.McpServerService;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2026-07-19
 */
@Service
public class McpServerTestButtonHandler implements EruptButtonHandler<McpServer> {

    @Resource
    private McpServerService mcpServerService;

    @Override
    public String click(McpServer mcpServer, String[] params) {
        if (null == mcpServer.getServerType()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("Connection Protocol") + " " + I18nTranslate.$translate("erupt.notnull"));
        }
        if (null == mcpServer.getConfig() || mcpServer.getConfig().isBlank()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("Config") + " " + I18nTranslate.$translate("erupt.notnull"));
        }
        try (McpClient mcpClient = mcpServerService.buildMcpClient(mcpServer)) {
            List<ToolSpecification> tools = mcpClient.listTools();
            String toolNames = tools.stream().map(ToolSpecification::name).collect(Collectors.joining("\n"));
            return "alert(" + GsonFactory.getGson().toJson("Tools (" + tools.size() + "):\n" + toolNames) + ")";
        } catch (EruptWebApiRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

}
