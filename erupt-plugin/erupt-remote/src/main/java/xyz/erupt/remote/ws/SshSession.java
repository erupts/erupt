package xyz.erupt.remote.ws;

import com.google.gson.JsonObject;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.remote.model.RemoteHost;
import xyz.erupt.remote.util.SshConnector;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Browser ⇄ SSH shell bridge. Uses the same text protocol as erupt-terminal:
 * browser sends {@code {"type":"resize","cols":..,"rows":..}} and {@code {"type":"input","data":".."}},
 * the server streams terminal output back as text frames.
 *
 * @author YuePeng
 */
@Slf4j
public class SshSession implements RemoteBridge {

    @Getter
    private final Session ws;
    @Getter
    private final RemoteHost host;
    @Getter
    private final String account;
    private final String password;
    private final String privateKey;
    private final int connectTimeoutMs;

    @Getter
    private final long startTime = System.currentTimeMillis();
    @Getter
    private volatile long lastActivity = startTime;
    private volatile boolean closed;

    private final CountDownLatch sized = new CountDownLatch(1);
    private volatile int cols = 80;
    private volatile int rows = 24;

    private com.jcraft.jsch.Session ssh;
    private ChannelShell channel;
    private volatile OutputStream stdin;

    public SshSession(Session ws, RemoteHost host, String password, String privateKey,
                      String account, int connectTimeoutMs) {
        this.ws = ws;
        this.host = host;
        this.password = password;
        this.privateKey = privateKey;
        this.account = account;
        this.connectTimeoutMs = connectTimeoutMs;
    }

    @Override
    public void start() {
        Thread t = new Thread(this::run, "erupt-remote-ssh-" + ws.getId());
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void inbound(ByteBuffer buffer) {
        // SSH bridge speaks the JSON text protocol only
    }

    @Override
    public void inboundText(String message) {
        JsonObject msg;
        try {
            msg = GsonFactory.getGson().fromJson(message, JsonObject.class);
        } catch (Exception e) {
            return;
        }
        if (msg == null || !msg.has("type")) return;
        switch (msg.get("type").getAsString()) {
            case "resize" -> {
                cols = Math.max(1, msg.get("cols").getAsInt());
                rows = Math.max(1, msg.get("rows").getAsInt());
                sized.countDown();
                ChannelShell ch = channel;
                if (ch != null && ch.isConnected()) ch.setPtySize(cols, rows, 0, 0);
            }
            case "input" -> {
                lastActivity = System.currentTimeMillis();
                OutputStream out = stdin;
                if (out == null) return;
                try {
                    out.write(msg.get("data").getAsString().getBytes(StandardCharsets.UTF_8));
                    out.flush();
                } catch (IOException e) {
                    log.debug("[erupt-remote] SSH stdin write failed: {}", e.getMessage());
                }
            }
            default -> {
            }
        }
    }

    private void run() {
        try {
            ssh = SshConnector.connect(host, password, privateKey, connectTimeoutMs);

            // Wait briefly for the browser to report its terminal size so the first prompt is laid out correctly
            sized.await(3, TimeUnit.SECONDS);
            channel = (ChannelShell) ssh.openChannel("shell");
            channel.setPtyType("xterm-256color", cols, rows, 0, 0);
            Reader reader = new InputStreamReader(channel.getInputStream(), StandardCharsets.UTF_8);
            stdin = channel.getOutputStream();
            channel.connect(connectTimeoutMs);

            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sendText(new String(buf, 0, n));
            }
        } catch (JSchException e) {
            String m = e.getMessage() == null ? e.toString() : e.getMessage();
            if (SshConnector.isAuthFailure(e)) {
                close(CODE_AUTH_FAILED, m.contains("Auth") ? "SSH authentication failed" : "Host key rejected: " + m);
            } else {
                close(CODE_CONNECT_FAILED, "Connection failed: " + m);
            }
        } catch (Exception e) {
            if (!closed) {
                log.warn("[erupt-remote] SSH session to {}:{} failed: {}", host.getHost(), host.getPort(), e.toString());
                close(CODE_CONNECT_FAILED, "Connection failed: " + e.getMessage());
            }
        } finally {
            close(CloseReason.CloseCodes.NORMAL_CLOSURE.getCode(), "Session ended");
        }
    }


    private void sendText(String text) throws IOException {
        synchronized (ws) {
            if (ws.isOpen()) ws.getBasicRemote().sendText(text);
        }
    }

    @Override
    public void ping() {
        synchronized (ws) {
            if (ws.isOpen()) {
                try {
                    ws.getBasicRemote().sendPing(ByteBuffer.allocate(0));
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void close(int code, String reason) {
        if (closed) return;
        closed = true;
        if (channel != null) channel.disconnect();
        if (ssh != null) ssh.disconnect();
        synchronized (ws) {
            if (ws.isOpen()) {
                try {
                    String r = reason != null && reason.length() > 120 ? reason.substring(0, 120) : reason;
                    ws.close(new CloseReason(() -> code, r));
                } catch (IOException ignored) {
                }
            }
        }
    }

}
