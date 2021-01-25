package xyz.erupt.server.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.net.InetAddress;
import java.util.Properties;

/**
 * @author liyuepeng
 * @date 2021/1/23 23:26
 */
@Getter
@Setter
public class Sys {

    private String name; //系统名称

    private String arch; //架构

    private String ip;

    private String hostName;

    @SneakyThrows
    Sys() {
        InetAddress addr = InetAddress.getLocalHost();
        this.setIp(addr.getHostAddress());
        this.setHostName(addr.getHostName());
        Properties props = System.getProperties();
        this.setName(props.getProperty("os.name") + " " + props.getProperty("os.version"));
        this.setArch(props.getProperty("os.arch"));
    }
}
