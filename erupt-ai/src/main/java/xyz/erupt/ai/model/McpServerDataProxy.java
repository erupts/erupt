package xyz.erupt.ai.model;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.vo.mcp.McpClientInfo;
import xyz.erupt.ai.vo.mcp.McpServerSse;
import xyz.erupt.ai.vo.mcp.McpServerStdio;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.sub_edit.OnChange;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2026/3/24 22:53
 */
@Slf4j
@Component
public class McpServerDataProxy implements DataProxy<McpServer>, OnChange<McpServer> {

    @Resource
    private EruptDao eruptDao;

    @Getter
    private static final Map<Long, McpClientInfo> MCP_CLIENTS = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (McpServer mcpServer : eruptDao.lambdaQuery(McpServer.class).eq(McpServer::getEnable, true).list()) {
            register(mcpServer);
        }
    }

    @SneakyThrows
    private synchronized void unregister(McpServer mcpServer) {
        if (MCP_CLIENTS.containsKey(mcpServer.getId())) {
            McpClient mcpClient = MCP_CLIENTS.get(mcpServer.getId()).getMcpClient();
            if (null != mcpClient) {
                mcpClient.close();
            }
            MCP_CLIENTS.remove(mcpServer.getId());
        }
    }

    @SneakyThrows
    private synchronized void register(McpServer mcpServer) {
        long start = System.currentTimeMillis();
        this.unregister(mcpServer);
        if (mcpServer.getEnable()) {
            McpClientInfo mcpClientInfo = new McpClientInfo();
            switch (mcpServer.getServerType()) {
                case STDIO -> {
                    McpServerStdio stdio = GsonFactory.getGson().fromJson(mcpServer.getConfig(), McpServerStdio.class);
                    List<String> command = new ArrayList<>();
                    command.add(stdio.getCommand());
                    if (null != stdio.getArgs()) {
                        command.addAll(stdio.getArgs());
                    }
                    try {
                        StdioMcpTransport.Builder transportBuilder = new StdioMcpTransport.Builder()
                                .command(command)
                                .logEvents(true);
                        if (null != stdio.getEnv()) {
                            transportBuilder.environment(stdio.getEnv());
                        }
                        McpClient mcpClient = new DefaultMcpClient.Builder()
                                .clientName(mcpServer.getName())
                                .transport(transportBuilder.build())
                                .build();
                        mcpClientInfo.setName(mcpServer.getName());
                        mcpClientInfo.setMcpClient(mcpClient);
                    } catch (Exception e) {
                        mcpClientInfo.setError(e.getMessage());
                        log.error("Failed to initialize MCP client (STDIO): {}", mcpServer.getName(), e);
                    }
                }
                case SSE -> {
                    McpServerSse sse = GsonFactory.getGson().fromJson(mcpServer.getConfig(), McpServerSse.class);
                    try {
                        McpClient mcpClient = new DefaultMcpClient.Builder()
                                .clientName(mcpServer.getName())
                                .transport(new StreamableHttpMcpTransport.Builder().url(sse.getUrl())
                                        .customHeaders(sse.getHeaders()).build())
                                .build();
                        mcpClientInfo.setMcpClient(mcpClient);
                    } catch (Exception e) {
                        mcpClientInfo.setError(e.getMessage());
                        log.error("Failed to initialize MCP client (SSE): {}", mcpServer.getName(), e);
                    }
                }
            }
            MCP_CLIENTS.put(mcpServer.getId(), mcpClientInfo);
            log.info("MCP server {} registered in {}ms", mcpServer.getName(), System.currentTimeMillis() - start);
        }
    }

    @PreDestroy
    public void destroy() {
        MCP_CLIENTS.values().forEach(it -> {
            try {
                if (null != it.getMcpClient()) {
                    it.getMcpClient().close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        for (Map<String, Object> map : list) {
            if ((boolean) map.get(LambdaSee.field(McpServer::getEnable))) {
                McpClientInfo mcpClient = MCP_CLIENTS.get(Long.valueOf(map.get(LambdaSee.field(McpServer::getId)).toString()));
                if (null != mcpClient.getError()) {
                    map.put(LambdaSee.field(McpServer::getTools), "<span style='color: red'>" + mcpClient.getError() + "</span>");
                } else {
                    try {
                        map.put(LambdaSee.field(McpServer::getTools), "<div style='display: flex; gap: 8px; flex-wrap: wrap;'>" +
                                mcpClient.getMcpClient().listTools().stream().map((it) ->
                                                "<span class='tool-item' style='background: #f0f0f0; padding: 2px 8px; border-radius: 4px; font-size: 12px; border: 1px solid #ddd; cursor: help;' title='" + (it.description() == null ? "" : it.description()) + "'>" + it.name() + "</span>")
                                        .collect(Collectors.joining("")) + "</div>");
                    } catch (Exception e) {
                        log.error("Failed to list tools for MCP client: {}", map.get(LambdaSee.field(McpServer::getName)), e);
                    }
                }
            }
        }
    }

    @Override
    public void afterAdd(McpServer mcpServer) {
        this.register(mcpServer);
    }

    @Override
    public void afterUpdate(McpServer mcpServer) {
        this.register(mcpServer);
    }

    @Override
    public void afterDelete(McpServer mcpServer) {
        this.unregister(mcpServer);
    }


    @Override
    public Map<String, Object> populateForm(McpServer mcpServer, String[] params) {
        Map<String, Object> data = new HashMap<>();
        if (null == mcpServer.getId()) {
            switch (mcpServer.getServerType()) {
                case SSE ->
                        data.put(LambdaSee.field(McpServer::getConfig), LLMDataProxy.gson.toJson(new McpServerSse()));
                case STDIO ->
                        data.put(LambdaSee.field(McpServer::getConfig), LLMDataProxy.gson.toJson(new McpServerStdio()));
            }
        }
        return data;
    }

}
