package xyz.erupt.webscoket.service;

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
public class EruptSocketService {

    @Resource
    private EruptContextService eruptContextService;

    public void executeJs(String script) {
        Session session = EruptSocketSessionManager.getSession(eruptContextService.getCurrentToken());
        session.getAsyncRemote().sendText("js," + script);
    }

}
