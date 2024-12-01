package xyz.erupt.webscoket.channel;

import org.springframework.stereotype.Component;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YuePeng
 * date 2024/12/1 12:14
 */
@Component
public class EruptSocketSessionManager {

    // <token,Session>
    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    // <sessionId,token>
    private static final Map<String, String> sessionTokens = new ConcurrentHashMap<>();

    public static void register(String token, Session session) {
        sessionMap.put(token, session);
        sessionTokens.put(session.getId(), token);
    }

    public static void close(Session session, CloseReason closeReason) {
        Optional.ofNullable(sessionTokens.remove(session.getId())).ifPresent(sessionMap::remove);

    }

    public static Session getSession(String token) {
        return sessionMap.get(token);
    }

}
