package xyz.erupt.monitor.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Date;
import java.util.Enumeration;

/**
 * @author YuePeng
 * date 2021/1/23 23:26
 */
@Getter
@Setter
public class Sys {

    private String name; //系统名称

    private String arch; //架构

    private String hostName; //主机名称

    private Date date; //服务器时间

    @SneakyThrows
    Sys() {
        InetAddress addr = InetAddress.getLocalHost();
        this.setHostName(addr.getHostName());
        OperatingSystemMXBean osb = ManagementFactory.getOperatingSystemMXBean();
        this.setName(osb.getName() + " " + osb.getVersion());
        this.setArch(osb.getArch());
        this.setDate(new Date());
    }

    //获取服务器IP地址
    @SneakyThrows
    public static String getServerIp() {
        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
        while (netInterfaces.hasMoreElements()) {
            InetAddress addr = netInterfaces.nextElement().getInetAddresses().nextElement();
            if (addr instanceof Inet4Address) {
                if (addr.isSiteLocalAddress() && !addr.isLoopbackAddress()) {
                    return addr.getHostAddress();
                }
            }
        }
        return InetAddress.getLocalHost().getHostAddress();
    }

}
