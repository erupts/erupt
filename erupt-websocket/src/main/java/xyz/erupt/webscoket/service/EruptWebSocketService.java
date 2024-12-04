package xyz.erupt.webscoket.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.webscoket.channel.EruptSocketSessionManager;
import xyz.erupt.webscoket.constant.EruptWebsocketConst;

import javax.annotation.Resource;
import javax.websocket.Session;
import java.util.Arrays;
import java.util.List;

/**
 * @author YuePeng
 * date 2024/12/1 15:39
 */
@Service
public class EruptWebSocketService {

    @Resource
    private EruptContextService eruptContextService;

    public List<Session> getCurrentSession() {
        return EruptSocketSessionManager.getSession(eruptContextService.getCurrentToken());
    }

    public void executeJs(String script) {
        for (Session session : getCurrentSession()) {
            this.executeJs(session, script);
        }
    }

    @SneakyThrows
    public void executeJs(Session session, String script) {
        session.getBasicRemote().sendText(GsonFactory.getGson().toJson(Arrays.asList(EruptWebsocketConst.COMMAND_JS, script)));
    }


}
