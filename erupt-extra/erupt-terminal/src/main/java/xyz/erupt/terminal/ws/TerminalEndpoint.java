package xyz.erupt.terminal.ws;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.terminal.EruptTerminalAutoConfiguration;
import xyz.erupt.upms.service.EruptTokenService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket endpoint — bridges a PTY shell (via system `script` command) to the browser terminal.
 *
 * @author YuePeng
 */
@Component
@ServerEndpoint("/erupt-terminal")
public class TerminalEndpoint {

    private static final Logger log = LoggerFactory.getLogger(TerminalEndpoint.class);

    // Session state before shell is started
    private static final Map<String, String> pendingTokenMap = new ConcurrentHashMap<>();
    private static final Map<String, Process> sessionProcessMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        List<String> tokens = session.getRequestParameterMap().get(EruptMutualConst.TOKEN);
        if (tokens == null || !EruptSpringUtil.getBean(EruptTokenService.class).tokenExist(tokens.get(0))) {
            session.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, "Unauthorized"));
            return;
        }
        // Defer shell start until the first resize message so we know the actual terminal size
        pendingTokenMap.put(session.getId(), tokens.get(0));
    }

    @OnMessage
    @SuppressWarnings("unchecked")
    public void onMessage(Session session, String message) {
        try {
            Map<String, Object> msg = GsonFactory.getGson().fromJson(message, Map.class);
            String type = (String) msg.get("type");

            // First resize message — start the shell with the correct dimensions
            if ("resize".equals(type) && pendingTokenMap.containsKey(session.getId())) {
                pendingTokenMap.remove(session.getId());
                int cols = ((Number) msg.get("cols")).intValue();
                int rows = ((Number) msg.get("rows")).intValue();
                Process process = buildShell(cols, rows);
                sessionProcessMap.put(session.getId(), process);
                sendBanner(session);
                new Thread(() -> pipeOutput(process.getInputStream(), session)).start();
                return;
            }

            Process process = sessionProcessMap.get(session.getId());
            if (process == null || !process.isAlive()) return;

            if ("input".equals(type)) {
                OutputStream stdin = process.getOutputStream();
                stdin.write(((String) msg.get("data")).getBytes(StandardCharsets.UTF_8));
                stdin.flush();
            }
            // Subsequent resize events are ignored — `script` owns the PTY fd
        } catch (IOException e) {
            log.warn("[terminal] Write to shell failed: {}", e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        pendingTokenMap.remove(session.getId());
        destroy(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable t) {
        log.error("[terminal] Session error id={}: {}", session.getId(), t.getMessage());
        pendingTokenMap.remove(session.getId());
        destroy(session.getId());
    }

    private void sendBanner(Session session) {
        try {
            String hostname = "unknown";
            try { hostname = InetAddress.getLocalHost().getHostName(); } catch (Exception ignored) {}
            String version = EruptTerminalAutoConfiguration.class.getPackage().getImplementationVersion();
            String versionSuffix = version != null ? "  \u001b[38;5;240mv" + version + "\u001b[0m" : "";
            String banner =
                "\r\n" +
                "\u001b[38;5;99m ███████╗██████╗ ██╗   ██╗██████╗ ████████╗\u001b[0m" + versionSuffix + "\r\n" +
                "\u001b[38;5;99m ██╔════╝██╔══██╗██║   ██║██╔══██╗╚══██╔══╝\u001b[0m\r\n" +
                "\u001b[38;5;99m █████╗  ██████╔╝██║   ██║██████╔╝   ██║   \u001b[0m\r\n" +
                "\u001b[38;5;99m ██╔══╝  ██╔══██╗██║   ██║██╔═══╝    ██║   \u001b[0m\r\n" +
                "\u001b[38;5;99m ███████╗██║  ██║╚██████╔╝██║        ██║   \u001b[0m\r\n" +
                "\u001b[38;5;99m ╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚═╝        ╚═╝   \u001b[0m\r\n" +
                "\r\n" +
                " \u001b[38;5;99mLow-Code · Zero-Frontend · Open Source\u001b[0m\r\n" +
                " Host \u001b[38;5;220m" + hostname + "\u001b[0m" +
                "  ·  OS \u001b[38;5;220m" + System.getProperty("os.name") + " " + System.getProperty("os.arch") + "\u001b[0m" +
                "  ·  Java \u001b[38;5;220m" + System.getProperty("java.version") + "\u001b[0m\r\n" +
                "\r\n";
            session.getBasicRemote().sendText(banner);
        } catch (IOException e) {
            log.warn("[terminal] Failed to send banner: {}", e.getMessage());
        }
    }

    private void pipeOutput(InputStream input, Session session) {
        byte[] buf = new byte[1024];
        int len;
        try {
            while ((len = input.read(buf)) != -1) {
                String chunk = new String(buf, 0, len, StandardCharsets.UTF_8);
                synchronized (session) {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendText(chunk);
                    }
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (session.isOpen()) {
                try {
                    session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Process exited"));
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void destroy(String sessionId) {
        Process process = sessionProcessMap.remove(sessionId);
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    private Process buildShell(int cols, int rows) throws IOException {
        Map<String, String> env = new HashMap<>(System.getenv());
        env.put("TERM", "xterm-256color");
        env.put("COLORTERM", "truecolor");
        env.put("COLUMNS", String.valueOf(cols));
        env.put("LINES", String.valueOf(rows));
        String os = System.getProperty("os.name", "").toLowerCase();
        ProcessBuilder pb;
        if (os.contains("win")) {
            pb = new ProcessBuilder("cmd.exe");
        } else if (os.contains("mac")) {
            pb = new ProcessBuilder("script", "-q", "/dev/null", "/bin/bash", "-l");
        } else {
            pb = new ProcessBuilder("script", "-q", "-f", "-c", "/bin/bash -l", "/dev/null");
        }
        pb.environment().putAll(env);
        pb.redirectErrorStream(true);
        return pb.start();
    }
}
