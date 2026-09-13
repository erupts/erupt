package xyz.erupt.webscoket.channel;

import jakarta.websocket.Session;
import org.springframework.stereotype.Component;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.upms.service.EruptUserService;
import xyz.erupt.webscoket.model.EruptWsSessionModel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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

    // <sessionId,sender> serialized async writer per connection
    private static final Map<String, EruptWsSender> senderMap = new ConcurrentHashMap<>();

    // <userId,tokens> index for user-targeted push
    private static final Map<Long, Set<String>> userTokenMap = new ConcurrentHashMap<>();

    public static void register(String token, Session session) {
        senderMap.put(session.getId(), new EruptWsSender(session));
        sessionMap.compute(token, (k, model) -> {
            if (null == model) {
                model = new EruptWsSessionModel(EruptSpringUtil.getBean(EruptUserService.class)
                        .getSimpleUserInfoByToken(token), new CopyOnWriteArrayList<>());
            }
            model.getSessions().add(session);
            userTokenMap.computeIfAbsent(model.getMetaUserinfo().getId(), id -> ConcurrentHashMap.newKeySet()).add(token);
            return model;
        });
        sessionTokenMap.put(session.getId(), token);
    }

    public static void close(Session session) {
        senderMap.remove(session.getId());
        Optional.ofNullable(sessionTokenMap.remove(session.getId())).ifPresent(token ->
                sessionMap.computeIfPresent(token, (k, model) -> {
                    model.getSessions().remove(session);
                    if (!model.getSessions().isEmpty()) return model;
                    userTokenMap.computeIfPresent(model.getMetaUserinfo().getId(), (id, tokens) -> {
                        tokens.remove(token);
                        return tokens.isEmpty() ? null : tokens;
                    });
                    return null;
                })
        );
    }

    public static void send(Session session, String text) {
        Optional.ofNullable(senderMap.get(session.getId())).ifPresent(sender -> sender.send(text));
    }

    public static EruptWsSessionModel getSession(String token) {
        return sessionMap.get(token);
    }

    public static List<EruptWsSessionModel> getSessionsByUser(Long userId) {
        return Optional.ofNullable(userTokenMap.get(userId)).map(tokens -> tokens.stream()
                .map(sessionMap::get).filter(Objects::nonNull).collect(Collectors.toList())
        ).orElse(Collections.emptyList());
    }

    public static List<EruptWsSessionModel> getAllSession() {
        return new ArrayList<>(sessionMap.values());
    }

}
