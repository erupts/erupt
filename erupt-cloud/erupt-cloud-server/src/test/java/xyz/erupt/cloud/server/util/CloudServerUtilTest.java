package xyz.erupt.cloud.server.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Failover is only safe when the connection never reached the node. These cases pin down exactly
 * which throwables count as "never reached" so a retry can't duplicate an already-processed write.
 *
 * @author YuePeng
 */
class CloudServerUtilTest {

    @Test
    void connectExceptions_areConnectFailures() {
        assertTrue(CloudServerUtil.isConnectFailure(new ConnectException("refused")));
        assertTrue(CloudServerUtil.isConnectFailure(new UnknownHostException("nope")));
        assertTrue(CloudServerUtil.isConnectFailure(new NoRouteToHostException("no route")));
    }

    @Test
    void nestedConnectException_isUnwrapped() {
        Exception wrapped = new RuntimeException("io error", new IOException("boom", new ConnectException("refused")));
        assertTrue(CloudServerUtil.isConnectFailure(wrapped));
    }

    @Test
    void readTimeoutAndAppErrors_areNotConnectFailures() {
        // A read timeout may mean the node already processed the request — must NOT fail over.
        assertFalse(CloudServerUtil.isConnectFailure(new SocketTimeoutException("read timed out")));
        assertFalse(CloudServerUtil.isConnectFailure(new RuntimeException("500 from node")));
        assertFalse(CloudServerUtil.isConnectFailure(null));
    }

}
