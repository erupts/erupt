package xyz.erupt.remote.ws;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xyz.erupt.remote.model.RemoteHost;
import xyz.erupt.remote.util.VncAuth;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Browser ⇄ VNC server bridge.
 * <p>
 * The RFB handshake is performed here so that, when a password is configured, the server-side
 * "VNC Authentication" is answered on the browser's behalf and the browser only ever sees an
 * authentication-free RFB 3.8 handshake. Without a usable password the handshake is relayed
 * transparently and noVNC prompts the user. After the handshake both directions are pumped as-is.
 *
 * @author YuePeng
 */
@Slf4j
public class VncSession implements RemoteBridge, Runnable {

    private static final byte[] POISON = new byte[0];
    private static final byte[] RFB_37 = "RFB 003.007\n".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] RFB_38 = "RFB 003.008\n".getBytes(StandardCharsets.US_ASCII);
    private static final int SEC_TYPE_VNC_AUTH = 2;
    private static final int HANDSHAKE_TIMEOUT_MS = 10_000;

    @Getter
    private final Session ws;
    @Getter
    private final RemoteHost host;
    @Getter
    private final String account;
    private final String password;
    private final int connectTimeoutMs;

    private final BlockingQueue<byte[]> inbound = new LinkedBlockingQueue<>();
    private final ByteArrayOutputStream carry = new ByteArrayOutputStream();

    @Getter
    private final long startTime = System.currentTimeMillis();
    @Getter
    private volatile long lastActivity = startTime;
    private volatile boolean closed;
    private Socket socket;

    public VncSession(Session ws, RemoteHost host, String password, String account, int connectTimeoutMs) {
        this.ws = ws;
        this.host = host;
        this.password = password;
        this.account = account;
        this.connectTimeoutMs = connectTimeoutMs;
    }

    @Override
    public void start() {
        Thread t = new Thread(this, "erupt-remote-vnc-" + ws.getId());
        t.setDaemon(true);
        t.start();
    }

    /** Called from the WebSocket container thread for every binary frame sent by the browser. */
    @Override
    public void inbound(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        lastActivity = System.currentTimeMillis();
        inbound.offer(bytes);
    }

    @Override
    public void inboundText(String message) {
        // The RFB stream is binary only
    }

    @Override
    public void run() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host.getHost(), host.getPort()), connectTimeoutMs);
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(HANDSHAKE_TIMEOUT_MS);
            InputStream in = new BufferedInputStream(socket.getInputStream());
            OutputStream out = socket.getOutputStream();
            handshake(in, out);
            socket.setSoTimeout(0);
            Thread reader = new Thread(() -> pumpServerToBrowser(in), "erupt-remote-rx-" + ws.getId());
            reader.setDaemon(true);
            reader.start();
            pumpBrowserToServer(out);
        } catch (AuthException e) {
            close(CODE_AUTH_FAILED, e.getMessage());
        } catch (SocketTimeoutException e) {
            close(CODE_CONNECT_FAILED, "Timed out talking to " + host.getHost() + ":" + host.getPort());
        } catch (Exception e) {
            if (!closed) {
                log.warn("[erupt-remote] Session to {}:{} failed: {}", host.getHost(), host.getPort(), e.toString());
                close(CODE_CONNECT_FAILED, "Connection failed: " + e.getMessage());
            }
        } finally {
            close(CloseReason.CloseCodes.NORMAL_CLOSURE.getCode(), "Session ended");
        }
    }

    // ---------------------------------------------------------------- handshake

    private void handshake(InputStream in, OutputStream out) throws Exception {
        byte[] serverVersion = readFully(in, 12);
        int minor = rfbMinor(serverVersion);
        if (minor < 7) {
            // RFB 3.3 negotiates differently; relay it untouched
            sendToBrowser(serverVersion);
            out.write(takeFromBrowser(12));
            out.flush();
            return;
        }
        out.write(minor >= 8 ? RFB_38 : RFB_37);
        out.flush();
        int count = in.read();
        if (count < 0) throw new EOFException("Server closed during handshake");
        if (count == 0) throw new AuthException("Server refused connection: " + readReason(in));
        byte[] types = readFully(in, count);

        if (password == null || !contains(types, SEC_TYPE_VNC_AUTH)) {
            // Transparent mode: replay what we consumed and let noVNC negotiate security itself
            sendToBrowser(serverVersion);
            takeFromBrowser(12);
            byte[] list = new byte[count + 1];
            list[0] = (byte) count;
            System.arraycopy(types, 0, list, 1, count);
            sendToBrowser(list);
            return;
        }

        // Authenticate against the VNC server on the browser's behalf
        out.write(SEC_TYPE_VNC_AUTH);
        out.flush();
        byte[] challenge = readFully(in, 16);
        out.write(VncAuth.respond(password, challenge));
        out.flush();
        int result = readU32(in);
        if (result != 0) {
            String reason = minor >= 8 ? readReason(in) : "";
            throw new AuthException("VNC authentication failed" + (reason.isEmpty() ? "" : ": " + reason));
        }

        // Present an authentication-free RFB 3.8 handshake to the browser
        sendToBrowser(RFB_38);
        takeFromBrowser(12);
        sendToBrowser(new byte[]{1, 1});
        takeFromBrowser(1);
        sendToBrowser(new byte[]{0, 0, 0, 0});
    }

    private static int rfbMinor(byte[] version) {
        // Format: "RFB xxx.yyy\n"; anything newer than 3.x (e.g. RealVNC 4.x, Apple 3.889) behaves like 3.8
        try {
            String s = new String(version, StandardCharsets.US_ASCII);
            int major = Integer.parseInt(s.substring(4, 7));
            int minor = Integer.parseInt(s.substring(8, 11));
            if (major > 3 || minor > 8) return 8;
            return minor;
        } catch (Exception e) {
            throw new IllegalStateException("Not an RFB server");
        }
    }

    private static boolean contains(byte[] types, int type) {
        for (byte b : types) if ((b & 0xff) == type) return true;
        return false;
    }

    private static String readReason(InputStream in) throws IOException {
        int len = readU32(in);
        if (len <= 0 || len > 4096) return "";
        return new String(readFully(in, len), StandardCharsets.UTF_8);
    }

    private static int readU32(InputStream in) throws IOException {
        byte[] b = readFully(in, 4);
        return ((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16) | ((b[2] & 0xff) << 8) | (b[3] & 0xff);
    }

    private static byte[] readFully(InputStream in, int n) throws IOException {
        byte[] buf = new byte[n];
        int off = 0;
        while (off < n) {
            int r = in.read(buf, off, n - off);
            if (r < 0) throw new EOFException("Server closed during handshake");
            off += r;
        }
        return buf;
    }

    /** Takes exactly {@code n} bytes sent by the browser; any surplus is kept for the relay phase. */
    private byte[] takeFromBrowser(int n) throws InterruptedException, IOException {
        while (carry.size() < n) {
            byte[] chunk = inbound.take();
            if (chunk == POISON || closed) throw new IOException("Browser disconnected during handshake");
            carry.write(chunk);
        }
        byte[] all = carry.toByteArray();
        carry.reset();
        carry.write(all, n, all.length - n);
        byte[] taken = new byte[n];
        System.arraycopy(all, 0, taken, 0, n);
        return taken;
    }

    // ---------------------------------------------------------------- relay

    private void pumpBrowserToServer(OutputStream out) throws IOException, InterruptedException {
        if (carry.size() > 0) {
            out.write(carry.toByteArray());
            out.flush();
            carry.reset();
        }
        while (!closed) {
            byte[] chunk = inbound.take();
            if (chunk == POISON) break;
            out.write(chunk);
            out.flush();
        }
    }

    private void pumpServerToBrowser(InputStream in) {
        byte[] buf = new byte[16 * 1024];
        try {
            int len;
            while ((len = in.read(buf)) != -1) {
                sendToBrowser(buf, 0, len);
            }
        } catch (IOException e) {
            if (!closed) log.debug("[erupt-remote] Server read ended: {}", e.getMessage());
        } finally {
            close(CloseReason.CloseCodes.NORMAL_CLOSURE.getCode(), "Server closed the connection");
        }
    }

    private void sendToBrowser(byte[] data) throws IOException {
        sendToBrowser(data, 0, data.length);
    }

    private void sendToBrowser(byte[] data, int off, int len) throws IOException {
        synchronized (ws) {
            if (ws.isOpen()) {
                ws.getBasicRemote().sendBinary(ByteBuffer.wrap(data, off, len));
            }
        }
    }

    // ---------------------------------------------------------------- lifecycle

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
        inbound.offer(POISON);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
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

    public boolean isClosed() {
        return closed;
    }

    private static class AuthException extends Exception {
        AuthException(String message) {
            super(message);
        }
    }
}
