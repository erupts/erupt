package xyz.erupt.terminal.ws;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.pty4j.WinSize;
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
import xyz.erupt.upms.service.EruptUserService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket endpoint — bridges a PTY shell (via pty4j) to the browser terminal.
 *
 * @author YuePeng
 */
@Component
@ServerEndpoint("/erupt-terminal")
public class TerminalEndpoint {

    private static final Logger log = LoggerFactory.getLogger(TerminalEndpoint.class);

    private static final long IDLE_TIMEOUT_MS = 30 * 60 * 1000L;

    private static final Map<String, String> pendingTokenMap = new ConcurrentHashMap<>();
    private static final Map<String, PtyProcess> sessionProcessMap = new ConcurrentHashMap<>();
    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastActivityMap = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService idleChecker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "erupt-terminal-idle");
        t.setDaemon(true);
        return t;
    });

    static {
        idleChecker.scheduleAtFixedRate(TerminalEndpoint::checkIdle, 1, 1, TimeUnit.MINUTES);
    }

    private static void checkIdle() {
        long now = System.currentTimeMillis();
        lastActivityMap.forEach((sid, last) -> {
            if (now - last > IDLE_TIMEOUT_MS) {
                Session s = sessionMap.get(sid);
                if (s != null && s.isOpen()) {
                    try {
                        s.close(new CloseReason(() -> 4000, "Idle timeout"));
                    } catch (IOException ignored) {}
                }
            }
        });
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        List<String> tokens = session.getRequestParameterMap().get(EruptMutualConst.TOKEN);
        if (tokens == null || !EruptSpringUtil.getBean(EruptTokenService.class).tokenExist(tokens.get(0))) {
            session.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, "Unauthorized"));
            return;
        }
        String token = tokens.get(0);
        if (EruptSpringUtil.getBean(EruptUserService.class)
                .getEruptMenuByValue(EruptTerminalAutoConfiguration.TERMINAL_KEY, token) == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Forbidden"));
            return;
        }
        sessionMap.put(session.getId(), session);
        pendingTokenMap.put(session.getId(), token);
    }

    @OnMessage
    @SuppressWarnings("unchecked")
    public void onMessage(Session session, String message) {
        try {
            Map<String, Object> msg = GsonFactory.getGson().fromJson(message, Map.class);
            String type = (String) msg.get("type");

            if ("resize".equals(type)) {
                int cols = ((Number) msg.get("cols")).intValue();
                int rows = ((Number) msg.get("rows")).intValue();

                if (pendingTokenMap.containsKey(session.getId())) {
                    // First resize — start the shell
                    pendingTokenMap.remove(session.getId());
                    PtyProcess pty;
                    try {
                        pty = buildShell(cols, rows);
                    } catch (Exception e) {
                        log.error("[terminal] Failed to start shell: {}", e.getMessage(), e);
                        session.getBasicRemote().sendText("\r\n\u001b[31m[ERROR] Failed to start shell: " + e.getMessage() + "\u001b[0m\r\n");
                        session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Shell start failed"));
                        return;
                    }
                    sessionProcessMap.put(session.getId(), pty);
                    lastActivityMap.put(session.getId(), System.currentTimeMillis());
                    sendBanner(session);
                    new Thread(() -> pipeOutput(pty.getInputStream(), session)).start();
                } else {
                    // Subsequent resize — directly set PTY window size via ioctl
                    PtyProcess pty = sessionProcessMap.get(session.getId());
                    if (pty != null && pty.isAlive()) {
                        pty.setWinSize(new WinSize(cols, rows));
                    }
                }
                return;
            }

            PtyProcess pty = sessionProcessMap.get(session.getId());
            if (pty == null || !pty.isAlive()) return;

            if ("input".equals(type)) {
                lastActivityMap.put(session.getId(), System.currentTimeMillis());
                OutputStream stdin = pty.getOutputStream();
                stdin.write(((String) msg.get("data")).getBytes(StandardCharsets.UTF_8));
                stdin.flush();
            }
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
            String now = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss", java.util.Locale.ENGLISH).format(new java.util.Date());
            String banner =
                "\r\nLast login: " + now + "\r\n" +
                "\r\n" +
                " \u001b[1;38;5;99m▶ ERUPT\u001b[0m" + versionSuffix +
                "  \u001b[38;5;240mLow-Code · Zero-Frontend · Open Source\u001b[0m\r\n" +
                " \u001b[38;5;240mHost \u001b[38;5;220m" + hostname + "\u001b[38;5;240m  OS \u001b[38;5;220m" + System.getProperty("os.name") + " " + System.getProperty("os.arch") + "\u001b[38;5;240m  Java \u001b[38;5;220m" + System.getProperty("java.version") + "\u001b[0m\r\n" +
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
                } catch (IOException ignored) {}
            }
        }
    }

    private void destroy(String sessionId) {
        sessionMap.remove(sessionId);
        lastActivityMap.remove(sessionId);
        PtyProcess pty = sessionProcessMap.remove(sessionId);
        if (pty != null && pty.isAlive()) {
            pty.destroyForcibly();
        }
    }

    private PtyProcess buildShell(int cols, int rows) throws IOException {
        Map<String, String> env = new HashMap<>(System.getenv());
        env.put("TERM", "xterm-256color");
        env.put("COLORTERM", "truecolor");
        String envShell = System.getenv("SHELL");
        String shell = isWindows() ? "cmd.exe"
                : (envShell != null && !envShell.contains("nologin") && !envShell.contains("false") ? envShell : "/bin/bash");
        return new PtyProcessBuilder()
                .setCommand(isWindows() ? new String[]{"cmd.exe"} : new String[]{shell})
                .setEnvironment(env)
                .setInitialColumns(cols)
                .setInitialRows(rows)
                .setConsole(false)
                .start();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
