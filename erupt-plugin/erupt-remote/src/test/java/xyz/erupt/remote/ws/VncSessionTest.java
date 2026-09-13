package xyz.erupt.remote.ws;

import jakarta.websocket.CloseReason;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.erupt.remote.model.RemoteHost;
import xyz.erupt.remote.util.VncAuth;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Drives {@link VncSession} against an in-process RFB 3.8 server and a mocked WebSocket session.
 */
class VncSessionTest {

    private static final byte[] RFB_38 = "RFB 003.008\n".getBytes(StandardCharsets.US_ASCII);
    private static final String SERVER_NAME = "erupt-fake-vnc";
    private static final String PASSWORD = "erupt123";

    private FakeRfbServer server;
    private final ByteArrayOutputStream toBrowser = new ByteArrayOutputStream();
    private final AtomicBoolean open = new AtomicBoolean(true);
    private final AtomicReference<CloseReason> closeReason = new AtomicReference<>();
    private Session ws;

    @BeforeEach
    void setUp() throws IOException {
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        doAnswer(inv -> {
            ByteBuffer buf = inv.getArgument(0);
            byte[] bytes = new byte[buf.remaining()];
            buf.get(bytes);
            synchronized (toBrowser) {
                toBrowser.write(bytes);
            }
            return null;
        }).when(basic).sendBinary(any(ByteBuffer.class));
        ws = mock(Session.class);
        when(ws.getId()).thenReturn("test");
        when(ws.getBasicRemote()).thenReturn(basic);
        when(ws.isOpen()).thenAnswer(inv -> open.get());
        doAnswer(inv -> {
            closeReason.set(inv.getArgument(0));
            open.set(false);
            return null;
        }).when(ws).close(any(CloseReason.class));
    }

    @AfterEach
    void tearDown() throws IOException {
        if (server != null) server.close();
    }

    @Test
    void managedPasswordIsAnsweredServerSideAndBrowserSeesNoAuth() throws Exception {
        server = new FakeRfbServer(PASSWORD);
        VncSession session = start(PASSWORD);

        assertArrayEquals(RFB_38, read(12));
        session.inbound(ByteBuffer.wrap(RFB_38));
        assertArrayEquals(new byte[]{1, 1}, read(2));
        session.inbound(ByteBuffer.wrap(new byte[]{1}));
        assertArrayEquals(new byte[]{0, 0, 0, 0}, read(4));

        assertServerInitAfterClientInit(session);
        assertRelay(session);
        session.close(1000, "done");
    }

    @Test
    void noStoredPasswordRelaysHandshakeTransparently() throws Exception {
        server = new FakeRfbServer(PASSWORD);
        VncSession session = start(null);

        assertArrayEquals(RFB_38, read(12));
        session.inbound(ByteBuffer.wrap(RFB_38));
        assertArrayEquals(new byte[]{1, 2}, read(2));
        session.inbound(ByteBuffer.wrap(new byte[]{2}));
        byte[] challenge = read(16);
        session.inbound(ByteBuffer.wrap(VncAuth.respond(PASSWORD, challenge)));
        assertArrayEquals(new byte[]{0, 0, 0, 0}, read(4));

        assertServerInitAfterClientInit(session);
        assertRelay(session);
        session.close(1000, "done");
    }

    @Test
    void wrongStoredPasswordClosesWithAuthFailedCode() throws Exception {
        server = new FakeRfbServer(PASSWORD);
        start("wrong-password");
        CloseReason reason = awaitClose();
        assertEquals(RemoteBridge.CODE_AUTH_FAILED, reason.getCloseCode().getCode());
    }

    @Test
    void unreachableHostClosesWithConnectFailedCode() throws Exception {
        int freePort;
        try (ServerSocket probe = new ServerSocket(0)) {
            freePort = probe.getLocalPort();
        }
        RemoteHost host = host(freePort);
        new VncSession(ws, host, null, "tester", 2000).start();
        CloseReason reason = awaitClose();
        assertEquals(RemoteBridge.CODE_CONNECT_FAILED, reason.getCloseCode().getCode());
    }

    // ------------------------------------------------------------------ helpers

    private VncSession start(String password) {
        VncSession session = new VncSession(ws, host(server.port()), password, "tester", 2000);
        session.start();
        return session;
    }

    private static RemoteHost host(int port) {
        RemoteHost host = new RemoteHost();
        host.setName("fake");
        host.setHost("127.0.0.1");
        host.setPort(port);
        return host;
    }

    private void assertServerInitAfterClientInit(VncSession session) throws Exception {
        session.inbound(ByteBuffer.wrap(new byte[]{1}));
        byte[] head = read(2 + 2 + 16 + 4);
        int width = ((head[0] & 0xff) << 8) | (head[1] & 0xff);
        int nameLen = ByteBuffer.wrap(head, 20, 4).getInt();
        assertEquals(640, width);
        assertEquals(SERVER_NAME, new String(read(nameLen), StandardCharsets.UTF_8));
    }

    private void assertRelay(VncSession session) throws Exception {
        byte[] payload = "ping-through-bridge".getBytes(StandardCharsets.US_ASCII);
        session.inbound(ByteBuffer.wrap(payload));
        assertArrayEquals(payload, read(payload.length));
    }

    private byte[] read(int n) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            synchronized (toBrowser) {
                if (toBrowser.size() >= n) {
                    byte[] all = toBrowser.toByteArray();
                    toBrowser.reset();
                    toBrowser.write(all, n, all.length - n);
                    return Arrays.copyOf(all, n);
                }
            }
            Thread.sleep(10);
        }
        throw new AssertionError("Timed out waiting for " + n + " bytes from the bridge; close=" + closeReason.get());
    }

    private CloseReason awaitClose() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (closeReason.get() == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertNotNull(closeReason.get(), "bridge did not close the WebSocket");
        return closeReason.get();
    }

    /** RFB 3.8 server: optional VNC authentication, fixed ServerInit, then echoes everything it receives. */
    private static final class FakeRfbServer implements AutoCloseable {

        private final ServerSocket serverSocket;
        private final String password;

        FakeRfbServer(String password) throws IOException {
            this.password = password;
            this.serverSocket = new ServerSocket(0);
            Thread t = new Thread(this::serve, "fake-rfb");
            t.setDaemon(true);
            t.start();
        }

        int port() {
            return serverSocket.getLocalPort();
        }

        private void serve() {
            try (Socket socket = serverSocket.accept()) {
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.write(RFB_38);
                in.readFully(new byte[12]);
                if (password != null) {
                    out.write(new byte[]{1, 2});
                    if (in.readUnsignedByte() != 2) return;
                    byte[] challenge = new byte[16];
                    new SecureRandom().nextBytes(challenge);
                    out.write(challenge);
                    byte[] response = new byte[16];
                    in.readFully(response);
                    if (!Arrays.equals(response, VncAuth.respond(password, challenge))) {
                        byte[] reason = "Bad password".getBytes(StandardCharsets.UTF_8);
                        out.writeInt(1);
                        out.writeInt(reason.length);
                        out.write(reason);
                        out.flush();
                        return;
                    }
                } else {
                    out.write(new byte[]{1, 1});
                    in.readUnsignedByte();
                }
                out.writeInt(0);
                in.readUnsignedByte(); // ClientInit
                out.writeShort(640);
                out.writeShort(400);
                out.write(new byte[]{32, 24, 0, 1, 0, (byte) 255, 0, (byte) 255, 0, (byte) 255, 16, 8, 0, 0, 0, 0});
                byte[] name = SERVER_NAME.getBytes(StandardCharsets.UTF_8);
                out.writeInt(name.length);
                out.write(name);
                out.flush();
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                    out.flush();
                }
            } catch (Exception ignored) {
            }
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
        }
    }
}
