package xyz.erupt.remote.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.remote.config.EruptRemoteProp;
import xyz.erupt.remote.ws.RemoteBridge;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Tracks live desktop sessions, enforces the concurrency limit and reaps idle sessions.
 *
 * @author YuePeng
 */
@Slf4j
@Component
public class RemoteSessionRegistry {

    @Resource
    private EruptRemoteProp prop;

    private final Map<String, RemoteBridge> sessions = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "erupt-remote-reaper");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::reapIdle, 1, 1, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(() -> sessions.values().forEach(RemoteBridge::ping), 30, 30, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        scheduler.shutdownNow();
        sessions.values().forEach(s -> s.close(1001, "Server shutting down"));
        sessions.clear();
    }

    public boolean hasCapacity() {
        return sessions.size() < prop.getMaxSessions();
    }

    public void register(RemoteBridge session) {
        sessions.put(session.getWs().getId(), session);
        log.info("[erupt-remote] {} connected to {} ({}:{}), active sessions: {}", session.getAccount(),
                session.getHost().getName(), session.getHost().getHost(), session.getHost().getPort(), sessions.size());
    }

    public RemoteBridge get(String wsId) {
        return sessions.get(wsId);
    }

    public void remove(String wsId, String reason) {
        RemoteBridge session = sessions.remove(wsId);
        if (session == null) return;
        session.close(1000, reason);
        long seconds = (System.currentTimeMillis() - session.getStartTime()) / 1000;
        log.info("[erupt-remote] {} disconnected from {} after {}s ({}), active sessions: {}", session.getAccount(),
                session.getHost().getName(), seconds, reason, sessions.size());
    }

    private void reapIdle() {
        long limit = prop.getIdleTimeoutMinutes() * 60_000L;
        long now = System.currentTimeMillis();
        sessions.values().stream()
                .filter(s -> now - s.getLastActivity() > limit)
                .forEach(s -> s.close(RemoteBridge.CODE_IDLE_TIMEOUT, "Idle timeout"));
    }
}
