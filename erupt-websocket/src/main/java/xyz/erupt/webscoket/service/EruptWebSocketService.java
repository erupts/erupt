package xyz.erupt.webscoket.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.webscoket.channel.EruptSocketSessionManager;

import javax.annotation.Resource;
import javax.websocket.Session;

/**
 * @author YuePeng
 * date 2024/12/1 15:39
 */
@Service
public class EruptWebSocketService {

    @Resource
    private EruptContextService eruptContextService;

    @SneakyThrows
    public void executeJs(String script) {
        Session session = EruptSocketSessionManager.getSession(eruptContextService.getCurrentToken());
        session.getBasicRemote().sendText("JS:" + script);
//        session.getAsyncRemote().sendText("js," + script);
    }

}
