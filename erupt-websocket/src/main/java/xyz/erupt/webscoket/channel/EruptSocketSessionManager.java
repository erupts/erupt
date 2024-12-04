package xyz.erupt.webscoket.channel;

import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YuePeng
 * date 2024/12/1 12:14
 */
@Component
public class EruptSocketSessionManager {

    // <token,Session>
    private static final Map<String, List<Session>> sessionMap = new ConcurrentHashMap<>();

    // <sessionId,token>
    private static final Map<String, String> sessionTokenMap = new ConcurrentHashMap<>();

    public static void register(String token, Session session) {
        sessionMap.computeIfAbsent(token, k -> new Vector<>());
        sessionMap.get(token).add(session);
        sessionTokenMap.put(session.getId(), token);
    }

    public static void close(Session session) {
        Optional.ofNullable(sessionTokenMap.remove(session.getId())).ifPresent(it->{
            if (sessionMap.get(it).size() <= 1) {
                sessionMap.remove(it);
            }else{
                sessionMap.get(it).remove(session);
            }
        });

    }

    public static List<Session> getSession(String token) {
        return sessionMap.get(token);
    }

}
