package xyz.erupt.webscoket.channel;

import jakarta.websocket.Session;
import org.springframework.stereotype.Component;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.webscoket.model.EruptWsSessionModel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YuePeng
 * date 2024/12/1 12:14
 */
@Component
public class EruptChannelManager {

    // <token,Session>
    private static final Map<String, EruptWsSessionModel> sessionMap = new ConcurrentHashMap<>();

    // <sessionId,token>
    private static final Map<String, String> sessionTokenMap = new ConcurrentHashMap<>();

    public static void register(String token, Session session) {
        sessionMap.computeIfAbsent(token, k -> new EruptWsSessionModel(EruptSpringUtil.getBean(EruptUserService.class)
                .getSimpleUserInfoByToken(token), new Vector<>()));
        sessionMap.get(token).getSessions().add(session);
        sessionTokenMap.put(session.getId(), token);
    }

    public static void close(Session session) {
        Optional.ofNullable(sessionTokenMap.remove(session.getId())).ifPresent(it -> {
            if (sessionMap.get(it).getSessions().size() <= 1) {
                sessionMap.remove(it);
            } else {
                sessionMap.get(it).getSessions().remove(session);
            }
        });

    }

    public static EruptWsSessionModel getSession(String token) {
        return sessionMap.get(token);
    }

    public static List<EruptWsSessionModel> getAllSession() {
        return new ArrayList<>(sessionMap.values());
    }

}
