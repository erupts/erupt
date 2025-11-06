package xyz.erupt.webscoket.channel;

import com.google.gson.reflect.TypeToken;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.upms.service.EruptTokenService;
import xyz.erupt.webscoket.command.base.SocketCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2024/11/30 23:44
 */
@Slf4j
@Component
@ServerEndpoint("/erupt")
public class EruptChannel {

    @SneakyThrows
    @OnOpen
    public void open(Session session) {
        List<String> token = session.getRequestParameterMap().get(EruptMutualConst.TOKEN);
        if (null == token || !EruptSpringUtil.getBean(EruptTokenService.class).tokenExist(token.get(0))) {
            session.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, "Token error"));
            return;
        }
        EruptChannelManager.register(token.get(0), session);
    }

    @OnMessage
    public void message(Session session, String message) {
        List<String> command = GsonFactory.getGson().fromJson(message, new TypeToken<ArrayList<String>>() {
        }.getType());
        SocketCommand<Object> socketCommand = SocketCommand.getCommand(command.get(0));
        try {
            socketCommand.handler(session, GsonFactory.getGson().fromJson(command.size() == 1 ? null : command.get(1), socketCommand.type));
        } catch (Exception e) {
            log.error("[websocket] Command execution error：{}", e.getMessage());
        }
    }

    @OnClose
    public void close(Session session, CloseReason closeReason) {
        if (closeReason.getCloseCode() != CloseReason.CloseCodes.NORMAL_CLOSURE && closeReason.getCloseCode() != CloseReason.CloseCodes.GOING_AWAY) {
            log.warn("[websocket] disconnect：id={}，{}", session.getId(), closeReason);
        }
        EruptChannelManager.close(session);
    }

    @OnError
    public void error(Session session, Throwable throwable) throws IOException {
        log.error("[websocket] Connection exception：id={}，throwable={}", session.getId(), throwable.getMessage());
        session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, throwable.getMessage()));
    }

}
