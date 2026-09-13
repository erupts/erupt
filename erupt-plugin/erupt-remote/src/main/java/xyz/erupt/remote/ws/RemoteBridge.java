package xyz.erupt.remote.ws;

import jakarta.websocket.Session;
import xyz.erupt.remote.model.RemoteHost;

import java.nio.ByteBuffer;

/**
 * One live browser session bridged to a remote host, regardless of protocol.
 *
 * @author YuePeng
 */
public interface RemoteBridge {

    int CODE_CONNECT_FAILED = 4005;
    int CODE_AUTH_FAILED = 4006;
    int CODE_IDLE_TIMEOUT = 4007;

    Session getWs();

    RemoteHost getHost();

    String getAccount();

    long getStartTime();

    long getLastActivity();

    /** Begin connecting to the remote host asynchronously. */
    void start();

    /** Binary frame from the browser. */
    void inbound(ByteBuffer buffer);

    /** Text frame from the browser. */
    void inboundText(String message);

    void ping();

    void close(int code, String reason);
}
