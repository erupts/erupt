package xyz.erupt.ai.service;

import com.google.gson.reflect.TypeToken;
import io.a2a.A2A;
import io.a2a.client.*;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfigBuilder;
import io.a2a.client.transport.spi.interceptors.ClientCallInterceptor;
import io.a2a.client.transport.spi.interceptors.PayloadAndHeaders;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import io.a2a.spec.Message;
import io.a2a.spec.TextPart;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.model.A2AAgent;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2026/5/15
 */
@Slf4j
@Component
public class A2AAgentService implements DataProxy<A2AAgent> {

    @Resource
    private EruptDao eruptDao;

    @Getter
    private static final Map<Long, AgentCard> AGENT_CARDS = new ConcurrentHashMap<>();
    @Getter
    private static final Map<Long, Client> AGENT_CLIENTS = new ConcurrentHashMap<>();
    private static final Map<Long, String> AGENT_ERRORS = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (A2AAgent agent : eruptDao.lambdaQuery(A2AAgent.class).eq(A2AAgent::getEnable, true).list()) {
            refresh(agent);
        }
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void check() {
        for (Long id : AGENT_ERRORS.keySet()) {
            A2AAgent agent = eruptDao.lambdaQuery(A2AAgent.class).eq(A2AAgent::getId, id).one();
            if (agent != null) {
                refresh(agent);
            }
        }
    }

    public void refresh(A2AAgent agent) {
        try {
            AgentCard card = A2A.getAgentCard(agent.getAgentUrl(),
                    "/.well-known/agent.json",
                    null == agent.getHeaders() ? null : GsonFactory.getGson().fromJson(agent.getHeaders(),
                            new TypeToken<Map<String, Object>>() {
                            }.getType()));
            Client client = buildClient(agent, card);
            AGENT_CARDS.put(agent.getId(), card);
            AGENT_CLIENTS.put(agent.getId(), client);
            AGENT_ERRORS.remove(agent.getId());
            log.info("A2A agent card loaded: {}", agent.getAgentUrl());
        } catch (Exception e) {
            AGENT_CARDS.remove(agent.getId());
            AGENT_CLIENTS.remove(agent.getId());
            AGENT_ERRORS.put(agent.getId(), e.getMessage());
            log.error("Failed to fetch A2A agent card: {}", agent.getAgentUrl(), e);
        }
    }

    private Client buildClient(A2AAgent agent, AgentCard card) {
        ClientBuilder builder = Client.builder(card)
                .clientConfig(new ClientConfig.Builder().setStreaming(false).build());
        JSONRPCTransportConfigBuilder transportBuilder = new JSONRPCTransportConfigBuilder();
        if (agent.getHeaders() != null && !agent.getHeaders().isBlank()) {
            Map<String, String> extraHeaders = GsonFactory.getGson().fromJson(
                    agent.getHeaders(), new TypeToken<Map<String, String>>() {}.getType());
            transportBuilder.addInterceptor(new ClientCallInterceptor() {
                @Override
                public PayloadAndHeaders intercept(String method, Object payload, Map<String, String> headers,
                                                   AgentCard agentCard, io.a2a.client.transport.spi.interceptors.ClientCallContext ctx) {
                    headers.putAll(extraHeaders);
                    return new PayloadAndHeaders(payload, headers);
                }
            });
        }
        return builder.withTransport(JSONRPCTransport.class, transportBuilder.build()).build();
    }

    /**
     * Send a message to an A2A agent and return the text response.
     */
    public String call(Client client, String message) throws Exception {
        Message msg = new Message.Builder()
                .role(Message.Role.USER)
                .parts(List.of(new TextPart(message)))
                .build();
        CompletableFuture<String> future = new CompletableFuture<>();
        client.sendMessage(msg, List.of((ClientEvent event, AgentCard card) -> {
            if (event instanceof MessageEvent me) {
                future.complete(me.getMessage().getParts().stream()
                        .filter(TextPart.class::isInstance).map(p -> ((TextPart) p).getText())
                        .collect(Collectors.joining("\n")));
            } else if (event instanceof TaskEvent te) {
                future.complete(te.getTask().getArtifacts().stream()
                        .flatMap(a -> a.parts().stream())
                        .filter(TextPart.class::isInstance).map(p -> ((TextPart) p).getText())
                        .collect(Collectors.joining("\n")));
            } else if (event instanceof TaskUpdateEvent tue && tue.getTask().getArtifacts() != null) {
                future.complete(tue.getTask().getArtifacts().stream()
                        .flatMap(a -> a.parts().stream())
                        .filter(TextPart.class::isInstance).map(p -> ((TextPart) p).getText())
                        .collect(Collectors.joining("\n")));
            }
        }), future::completeExceptionally, null);
        return future.get(30, TimeUnit.SECONDS);
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        String skillsKey = LambdaSee.field(A2AAgent::getSkills);
        String idKey = LambdaSee.field(A2AAgent::getId);
        String enableKey = LambdaSee.field(A2AAgent::getEnable);
        for (Map<String, Object> row : list) {
            if (!(boolean) row.get(enableKey)) {
                continue;
            }
            Long id = Long.valueOf(row.get(idKey).toString());
            String error = AGENT_ERRORS.get(id);
            if (error != null) {
                row.put(skillsKey, "<span style='color:red'>" + error + "</span>");
                continue;
            }
            AgentCard card = AGENT_CARDS.get(id);
            if (card != null && card.skills() != null) {
                row.put(skillsKey,
                        "<div style='display:flex;gap:8px;flex-wrap:wrap'>" +
                        card.skills().stream().map(this::skillBadge).collect(Collectors.joining()) +
                        "</div>");
            }
        }
    }

    private String skillBadge(AgentSkill skill) {
        String tooltip = skill.description() == null ? "" : skill.description().replace("'", "&#39;");
        return "<span style='background:#e6f7ff;padding:2px 8px;border-radius:4px;font-size:12px;" +
               "border:1px solid #91d5ff;cursor:help' title='" + tooltip + "'>" + skill.name() + "</span>";
    }

    @Override
    public void afterAdd(A2AAgent agent) {
        if (Boolean.TRUE.equals(agent.getEnable())) {
            refresh(agent);
        }
    }

    @Override
    public void afterUpdate(A2AAgent agent) {
        AGENT_CARDS.remove(agent.getId());
        AGENT_CLIENTS.remove(agent.getId());
        AGENT_ERRORS.remove(agent.getId());
        if (Boolean.TRUE.equals(agent.getEnable())) {
            refresh(agent);
        }
    }

    @Override
    public void afterDelete(A2AAgent agent) {
        AGENT_CARDS.remove(agent.getId());
        AGENT_CLIENTS.remove(agent.getId());
        AGENT_ERRORS.remove(agent.getId());
    }
}
