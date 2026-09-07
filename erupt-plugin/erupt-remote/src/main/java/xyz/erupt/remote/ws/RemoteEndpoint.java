package xyz.erupt.remote.ws;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.remote.config.EruptRemoteProp;
import xyz.erupt.remote.model.RemoteHost;
import xyz.erupt.remote.service.RemoteSessionRegistry;
import xyz.erupt.remote.service.RemoteTicketService;
import xyz.erupt.remote.util.RemoteCrypto;
import xyz.erupt.upms.service.EruptTokenService;
import xyz.erupt.upms.service.EruptUserService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * WebSocket endpoint that bridges a browser to a remote host (VNC desktop or SSH shell).
 * <pre>/erupt-remote?token=&lt;login token&gt;&amp;ticket=&lt;one-time ticket&gt;</pre>
 * The target host and protocol are resolved from the ticket only; the browser can never pick an arbitrary address.
 *
 * @author YuePeng
 */
@Slf4j
@Component
@ServerEndpoint("/erupt-remote")
public class RemoteEndpoint {

    private static final int CODE_INVALID_TICKET = 4001;
    private static final int CODE_HOST_UNAVAILABLE = 4002;
    private static final int CODE_FORBIDDEN = 4003;
    private static final int CODE_TOO_MANY_SESSIONS = 4004;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        Map<String, List<String>> params = session.getRequestParameterMap();
        String token = first(params.get(EruptMutualConst.TOKEN));
        String ticket = first(params.get("ticket"));
        if (token == null || !EruptSpringUtil.getBean(EruptTokenService.class).tokenExist(token)) {
            session.close(new CloseReason(() -> CODE_FORBIDDEN, "Unauthorized"));
            return;
        }
        RemoteTicketService.Ticket t = EruptSpringUtil.getBean(RemoteTicketService.class).consume(ticket, token);
        if (t == null) {
            session.close(new CloseReason(() -> CODE_INVALID_TICKET, "Invalid or expired ticket"));
            return;
        }
        EruptUserService userService = EruptSpringUtil.getBean(EruptUserService.class);
        if (userService.getEruptMenuByValue(RemoteHost.class.getSimpleName(), token) == null) {
            session.close(new CloseReason(() -> CODE_FORBIDDEN, "Forbidden"));
            return;
        }
        RemoteSessionRegistry registry = EruptSpringUtil.getBean(RemoteSessionRegistry.class);
        if (!registry.hasCapacity()) {
            session.close(new CloseReason(() -> CODE_TOO_MANY_SESSIONS, "Too many concurrent sessions"));
            return;
        }
        RemoteHost host = EruptSpringUtil.getBean(EruptDao.class).find(RemoteHost.class, t.hostId());
        if (host == null || !Boolean.TRUE.equals(host.getEnabled())) {
            session.close(new CloseReason(() -> CODE_HOST_UNAVAILABLE, "Host unavailable"));
            return;
        }
        RemoteCrypto crypto = EruptSpringUtil.getBean(RemoteCrypto.class);
        String password = crypto.decrypt(host.getPassword());
        String account = userService.getSimpleUserInfoByToken(token).getAccount();
        int timeoutMs = EruptSpringUtil.getBean(EruptRemoteProp.class).getConnectTimeoutSeconds() * 1000;

        session.setMaxIdleTimeout(0);
        session.setMaxBinaryMessageBufferSize(1024 * 1024);
        RemoteBridge bridge = RemoteHost.PROTOCOL_SSH.equals(host.getProtocol())
                ? new SshSession(session, host, host.getUsername(), password, crypto.decrypt(host.getPrivateKey()), account, timeoutMs)
                : new VncSession(session, host, password, account, timeoutMs);
        registry.register(bridge);
        bridge.start();
    }

    @OnMessage
    public void onBinary(Session session, ByteBuffer buffer) {
        RemoteBridge bridge = EruptSpringUtil.getBean(RemoteSessionRegistry.class).get(session.getId());
        if (bridge != null) bridge.inbound(buffer);
    }

    @OnMessage
    public void onText(Session session, String message) {
        RemoteBridge bridge = EruptSpringUtil.getBean(RemoteSessionRegistry.class).get(session.getId());
        if (bridge != null) bridge.inboundText(message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        EruptSpringUtil.getBean(RemoteSessionRegistry.class).remove(session.getId(),
                reason.getReasonPhrase() == null || reason.getReasonPhrase().isEmpty() ? "Browser disconnected" : reason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable t) {
        log.warn("[erupt-remote] Session {} error: {}", session.getId(), t.getMessage());
        EruptSpringUtil.getBean(RemoteSessionRegistry.class).remove(session.getId(), "Error: " + t.getMessage());
    }

    private static String first(List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }
}
