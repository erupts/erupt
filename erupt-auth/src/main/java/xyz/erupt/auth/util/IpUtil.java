package xyz.erupt.auth.util;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

/**
 * @author liyuepeng
 * @date 2018-12-24.
 */
public class IpUtil {
    public static String getIpAddr(HttpServletRequest request) {
        try {
            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0
                    || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0
                    || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0
                    || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
                if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();

                    // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
                    // "***.***.***.***".length()
                    if (ip != null && ip.length() > 15) {
                        // = 15
                        if (ip.indexOf(",") > 0) {
                            ip = ip.substring(0, ip.indexOf(","));
                        }
                    }
                }
            }
            // "***.***.***.***".length()
            if (ip != null && ip.length() > 15) {
                // = 15
                if (ip.indexOf(",") > 0) {
                    ip = ip.substring(0, ip.indexOf(","));
                }
            }
            return ip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
