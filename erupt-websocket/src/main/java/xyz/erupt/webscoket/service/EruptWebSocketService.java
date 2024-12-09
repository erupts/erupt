package xyz.erupt.webscoket.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.webscoket.channel.EruptChannelManager;
import xyz.erupt.webscoket.channel.SocketCommand;

import javax.annotation.Resource;
import javax.websocket.Session;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2024/12/1 15:39
 */
@Service
public class EruptWebSocketService {

    @Resource
    private EruptContextService eruptContextService;

    //获取所有已连接会话
    public List<Session> getAllSession() {
        return EruptChannelManager.getAllSession();
    }

    //获取当前操作用户的会话
    public List<Session> getCurrentSession() {
        return Optional.ofNullable(EruptChannelManager.getSession(eruptContextService.getCurrentToken())).orElse(new ArrayList<>());
    }

    @SneakyThrows
    public <T> void send(List<Session> sessions, SocketCommand command, T data) {
        for (Session session : sessions) {
            session.getBasicRemote().sendText(GsonFactory.getGson().toJson(Arrays.asList(command, GsonFactory.getGson().toJson(data))));
        }
    }

    @SneakyThrows
    public <T> void send(SocketCommand command, T data) {
        for (Session session : getCurrentSession()) {
            session.getBasicRemote().sendText(GsonFactory.getGson().toJson(Arrays.asList(command, GsonFactory.getGson().toJson(data))));
        }
    }

    @SneakyThrows
    public void sendMessage(String message) {
        this.send(SocketCommand.JS, "window.msg.info('" + message + "')");
    }


}
