package xyz.erupt.webscoket.command.base;

import javax.websocket.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2024/12/1 12:35
 */
public abstract class SocketCommand<T> {

    public static  Map<String, SocketCommand<?>> socketCommandMap = new HashMap<>();
    public Class<?> type;

    public SocketCommand() {
        this.type = (Class<?>) ((java.lang.reflect.ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        socketCommandMap.put(this.command(), this);
    }

    public static <S> SocketCommand<S> getCommand(String command) {
        return (SocketCommand<S>) socketCommandMap.get(command);
    }

    protected abstract String command();

    public abstract void handler(Session session, T message);
}
