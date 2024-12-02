package xyz.erupt.webscoket.channel;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.upms.service.EruptTokenService;
import xyz.erupt.webscoket.command.base.SocketCommand;
import xyz.erupt.webscoket.constant.EruptWebsocketConst;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;

/**
 * @author YuePeng
 * date 2024/11/30 23:44
 */
@Slf4j
@Component
@ServerEndpoint(value = "/erupt")
public class EruptChannel {

    @SneakyThrows
    @OnOpen
    public void onOpen(Session session) {
        List<String> token = session.getRequestParameterMap().get("token");
        if (null == token || !EruptSpringUtil.getBean(EruptTokenService.class).tokenExist(token.get(0))) {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Token error"));
            return;
        }
        EruptSocketSessionManager.register(token.get(0), session);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        int commandIndex = message.contains(EruptWebsocketConst.COMMAND_SYNTAX) ? message.indexOf(EruptWebsocketConst.COMMAND_SYNTAX) : message.length();
        String command = message.substring(0, commandIndex);
        String data = message.substring(commandIndex);
        SocketCommand<Object> socketCommand = SocketCommand.getCommand(command);
        socketCommand.handler(session, GsonFactory.getGson().fromJson(data, socketCommand.type));
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        if (closeReason.getCloseCode() != CloseReason.CloseCodes.NORMAL_CLOSURE) {
            log.warn("[websocket] disconnect：id={}，{}", session.getId(), closeReason);
        }
        EruptSocketSessionManager.close(session, closeReason);
    }

    // 连接异常
    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        log.error("[websocket] Connection exception：id={}，throwable={}", session.getId(), throwable.getMessage());
        session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, throwable.getMessage()));
    }

}
