package xyz.erupt.webscoket.service;

import jakarta.annotation.Resource;
import jakarta.websocket.Session;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.webscoket.channel.EruptChannelManager;
import xyz.erupt.webscoket.channel.SocketCommand;
import xyz.erupt.webscoket.model.EruptWsSessionModel;

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

    // Obtain all connected sessions
    public List<EruptWsSessionModel> getAllSession() {
        return EruptChannelManager.getAllSession();
    }

    // Obtain the current session of the operating user
    public EruptWsSessionModel getCurrentSession() {
        return Optional.ofNullable(EruptChannelManager.getSession(eruptContextService.getCurrentToken())).orElseThrow(() ->
                new EruptWebApiRuntimeException("not found websocket session")
        );
    }

    @SneakyThrows
    public <T> void send(EruptWsSessionModel eruptWsSessionModel, SocketCommand command, T data) {
        for (Session session : eruptWsSessionModel.getSessions()) {
            session.getBasicRemote().sendText(GsonFactory.getGson().toJson(Arrays.asList(command.getCommand(), data)));
        }
    }

    public <T> void send(SocketCommand command, T data) {
        this.send(getCurrentSession(), command, data);
    }

    public void sendJsMessage(String message) {
        this.send(SocketCommand.JS, "window.msg.info('" + message + "')");
    }

    public void sendJsNotify(String title, String message) {
        this.send(SocketCommand.JS, String.format("window.notify.info('%s', '%s')", title, message));
    }

}
