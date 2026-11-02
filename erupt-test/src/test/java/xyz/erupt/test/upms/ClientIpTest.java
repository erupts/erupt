package xyz.erupt.test.upms;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.upms.util.IpUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The address the login lock and the IP white list key on is only ever taken from a
 * forwarded header when the peer is a trusted proxy, and then the first hop no proxy vouched for.
 */
public class ClientIpTest extends EruptApplicationTests {

    @Test
    public void directPeerIgnoresForwardedHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.9");
        request.addHeader("X-Forwarded-For", "192.168.0.5");
        assertEquals("203.0.113.9", IpUtil.getIpAddr(request));
    }

    @Test
    public void trustedProxyYieldsTheHopItVouchedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        // the client forged the first entry; the proxy appended the real peer
        request.addHeader("X-Forwarded-For", "192.168.0.5, 198.51.100.7, 10.0.0.2");
        assertEquals("198.51.100.7", IpUtil.getIpAddr(request));
    }

    @Test
    public void trustedProxyWithoutHeaderIsItself() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.2");
        assertEquals("10.0.0.2", IpUtil.getIpAddr(request));
    }

}
