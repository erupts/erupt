package xyz.erupt.k8s_dashboard.terminal;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.k8s.operation.K8sPodLogHandler;
import xyz.erupt.k8s.service.EruptK8sClientService;
import xyz.erupt.upms.service.EruptTokenService;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridges a pod to the browser terminal: {@code exec} with a TTY, or a following log stream.
 * Output goes out as binary frames; the client sends {@code {"type":"input","data":...}} and
 * {@code {"type":"resize","cols":n,"rows":n}}, the protocol of erupt-terminal.
 *
 * @author YuePeng
 */
@Slf4j
@ServerEndpoint(K8sTerminalEndpoint.PATH)
public class K8sTerminalEndpoint {

    public static final String PATH = "/erupt-k8s-terminal";

    private static final long IDLE_TIMEOUT_MS = 30 * 60 * 1000L;

    private static final int LOG_TAIL = 500;

    // Prefers bash, falls back to sh: images ship either or both
    private static final String[] SHELL = {"sh", "-c", "if command -v bash >/dev/null 2>&1; then exec bash; else exec sh; fi"};

    private static final Map<String, Closeable> streams = new ConcurrentHashMap<>();

    private static final Map<String, ExecWatch> execs = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        List<String> ids = session.getRequestParameterMap().get("ticket");
        K8sTerminalTicketService.Ticket ticket = EruptSpringUtil.getBean(K8sTerminalTicketService.class)
                .consume(null == ids || ids.isEmpty() ? null : ids.get(0));
        // The ticket belongs to a session; a logged-out session takes its terminal with it
        if (null == ticket || !EruptSpringUtil.getBean(EruptTokenService.class).tokenExist(ticket.token())) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            return;
        }
        session.setMaxIdleTimeout(IDLE_TIMEOUT_MS);
        EruptK8sClientService clientService = EruptSpringUtil.getBean(EruptK8sClientService.class);
        KubernetesClient client = clientService.client(clientService.eruptK8s(ticket.eruptClass()));
        PodResource pod = K8sPodLogHandler.pods(client, new EruptK8sClientService.Target(ticket.namespace(), ticket.pod()));
        OutputStream out = new SessionOutputStream(session);
        try {
            if (ticket.mode() == K8sTerminalTicketService.Mode.LOGS) {
                LogWatch watch = pod.inContainer(ticket.container()).tailingLines(LOG_TAIL).watchLog(out);
                streams.put(session.getId(), watch);
            } else {
                ExecWatch watch = pod.inContainer(ticket.container())
                        .redirectingInput().writingOutput(out).writingError(out).withTTY()
                        .usingListener(new ExecListener() {
                            @Override
                            public void onClose(int code, String reason) {
                                closeQuietly(session, CloseReason.CloseCodes.NORMAL_CLOSURE, "Session ended");
                            }
                        })
                        .exec(SHELL);
                execs.put(session.getId(), watch);
                streams.put(session.getId(), watch::close);
            }
        } catch (RuntimeException e) {
            log.warn("erupt-k8s terminal failed to open {}/{}: {}", ticket.namespace(), ticket.pod(), e.getMessage());
            out.write(("\r\n\u001b[31m" + e.getMessage() + "\u001b[0m\r\n").getBytes(StandardCharsets.UTF_8));
            closeQuietly(session, CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Open failed");
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        ExecWatch watch = execs.get(session.getId());
        if (null == watch) return;
        try {
            JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
            String type = msg.has("type") ? msg.get("type").getAsString() : "";
            if ("input".equals(type) && msg.has("data")) {
                watch.getInput().write(msg.get("data").getAsString().getBytes(StandardCharsets.UTF_8));
                watch.getInput().flush();
            } else if ("resize".equals(type)) {
                watch.resize(msg.get("cols").getAsInt(), msg.get("rows").getAsInt());
            }
        } catch (Exception e) {
            log.debug("erupt-k8s terminal message dropped: {}", e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        this.release(session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.debug("erupt-k8s terminal error: {}", error.getMessage());
        this.release(session);
    }

    private void release(Session session) {
        execs.remove(session.getId());
        Closeable stream = streams.remove(session.getId());
        if (null != stream) {
            try {
                stream.close();
            } catch (IOException ignore) {
                // already gone
            }
        }
    }

    private static void closeQuietly(Session session, CloseReason.CloseCodes code, String reason) {
        try {
            if (session.isOpen()) session.close(new CloseReason(code, reason));
        } catch (IOException ignore) {
            // already closed
        }
    }

    // Pod output in binary frames: xterm decodes UTF-8 itself, so a split multi-byte char survives
    private static final class SessionOutputStream extends OutputStream {

        private final Session session;

        SessionOutputStream(Session session) {
            this.session = session;
        }

        @Override
        public void write(int b) throws IOException {
            this.write(new byte[]{(byte) b}, 0, 1);
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            if (!session.isOpen()) throw new IOException("terminal closed");
            synchronized (session) {
                session.getBasicRemote().sendBinary(ByteBuffer.wrap(bytes, offset, length));
            }
        }

    }

}
