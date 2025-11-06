package xyz.erupt.upms.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

/**
 * @author YuePeng
 * date 2018-12-24.
 */
@Slf4j
public class IpUtil {

    public static String getIpAddr(HttpServletRequest request) {
        try {
            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
                if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                    // Obtain the IP address of this machine from the network card.
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                    // For the case where multiple proxies are used, the first IP represents the client's actual IP, and the multiple IPs are separated by ','.
                    // "***.***.***.***".length()
                    if (ip != null && ip.length() > 15) {
                        // = 15
                        if (ip.indexOf(',') > 0) {
                            ip = ip.substring(0, ip.indexOf(','));
                        }
                    }
                }
            }
            // "***.***.***.***".length()
            if (ip != null && ip.length() > 15) {
                // = 15
                if (ip.indexOf(',') > 0) {
                    ip = ip.substring(0, ip.indexOf(','));
                }
            }
            return ip;
        } catch (Exception e) {
            log.warn("get ip error {}", e.getMessage());
            return null;
        }
    }

    private static byte[] fileByte;

    static {
        try (InputStream input = IpUtil.class.getClassLoader().getResourceAsStream("ip2region.db")) {
            if (null == input) {
                throw new RuntimeException("ip2region.db not found");
            }
            fileByte = StreamUtils.copyToByteArray(input);
        } catch (IOException e) {
            log.warn("ip2region load error", e);
        }
    }

    @SneakyThrows
    @SuppressWarnings("StringConcatenationArgumentToLogCall")
    public static String getCityInfo(String ip) {
        if (!Util.isIpAddress(ip)) {
            log.warn("Error: Invalid ip address: {}", ip);
            return "";
        }
        try {
            return new DbSearcher(new DbConfig(), fileByte).memorySearch(ip).getRegion();
        } catch (Exception e) {
            log.warn("ip2region error " + ip, e);
            return null;
        }
    }

}