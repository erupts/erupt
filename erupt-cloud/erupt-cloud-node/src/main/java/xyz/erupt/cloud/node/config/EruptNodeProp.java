package xyz.erupt.cloud.node.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2019-10-31.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(EruptNodeProp.SPACE)
public class EruptNodeProp {

    public static final String SPACE = "erupt.cloud-node";

    //是否开启NODE节点注册
    private boolean enableRegister = true;

    //接入应用名称，推荐填写当前 Java 项目名称
    private String nodeName;

    //客户端秘钥（在服务端界面生成）
    private String accessToken;

    //服务端地址（支持集群）
    private String[] serverAddresses;

    //自动注册时协议前缀
    private String schema = "http";

    /**
     * 当前服务地址（支持集群，非必填）
     * 正常情况下无需配置，多层代理等复杂网络环境下需配置此参数，目的是让server端准确寻址到node
     */
    private String[] hostAddress;

    //心跳时间(毫秒)
    private int heartbeatTime = 15 * 1000;

    private int count = 0;

    public String getBalanceAddress() {
        if (this.serverAddresses.length == 1) {
            return this.serverAddresses[0];
        }
        if (count >= Integer.MAX_VALUE) {
            count = 0;
        }
        String address = this.serverAddresses[count++ % this.serverAddresses.length];
        if (address.endsWith("/")) {
            return address.substring(0, address.length() - 1);
        }
        return address;
    }

}
