package xyz.erupt.webscoket.command;

import jakarta.websocket.Session;
import org.springframework.stereotype.Component;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.webscoket.command.base.SocketCommand;

import java.util.Collections;

/**
 * @author YuePeng
 * date 2024/12/1 12:39
 */
@Component
public class PingCommand extends SocketCommand<Void> {

    public PingCommand() {
        super();
    }

    @Override
    protected String command() {
        return "ping";
    }

    @Override
    public void handler(Session session, Void message) {
        session.getAsyncRemote().sendText(GsonFactory.getGson().toJson(Collections.singletonList("pong")));
    }

}
