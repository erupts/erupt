package xyz.erupt.upms.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;
import org.springframework.util.StreamUtils;

import jakarta.servlet.http.HttpServletRequest;
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
                    // 根据网卡取本机配置的IP
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                    // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
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
            log.warn("get ip error " + e.getMessage());
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
    public static String getCityInfo(String ip) {
        if (!Util.isIpAddress(ip)) {
            log.warn("Error: Invalid ip address: {}", ip);
            return "";
        }
        return new DbSearcher(new DbConfig(), fileByte).memorySearch(ip).getRegion();
    }

}