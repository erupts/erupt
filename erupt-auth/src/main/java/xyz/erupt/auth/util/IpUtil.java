package xyz.erupt.auth.util;

import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

/**
 * @author liyuepeng
 * @date 2018-12-24.
 */
@Log
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


    @SneakyThrows
    public static String getCityInfo(String ip) {
        if (!Util.isIpAddress(ip)) {
            log.warning("Error: Invalid ip address");
            return "";
        }
        //格式：国家|大区|省份|城市|运营商
        return new DbSearcher(new DbConfig(), IpUtil.class.getResource("/ip2region.db").getPath())
                .btreeSearch(ip).getRegion();
    }

}
