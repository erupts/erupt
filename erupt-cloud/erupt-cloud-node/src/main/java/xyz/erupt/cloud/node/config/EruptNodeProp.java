package xyz.erupt.cloud.node.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

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

    // Do you want to enable the registration of the NODE node?
    private boolean enableRegister = true;

    // Enter the application name. It is recommended to fill in the current name of your Java project.
    private String nodeName;

    // Client secret key (generated on the server interface)
    private String accessToken;

    // Server address (supporting clustering)
    private String[] serverAddresses;

    // Protocol prefix during automatic registration
    private String schema = "http";

    /**
     * Current service address (supports clustering, not mandatory)
     * In normal circumstances, no configuration is required. However, in complex network environments such as multi-layer proxies, this parameter needs to be configured. The purpose is to enable the server to accurately locate the node.
     */
    private String[] hostAddress;

    // Heartbeat time (milliseconds)
    private int heartbeatTime = 15 * 1000;

    // Graceful shutdown drain timeout (milliseconds): max wait for in-flight requests before exit
    private int drainTimeout = 30 * 1000;

    // Refuse server calls without a valid Erupt-Signature (the plain access token alone no longer passes)
    private boolean requireSignature = false;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final AtomicInteger serverCycle = new AtomicInteger();

    // the next server address, round-robin
    public String getBalanceAddress() {
        String address = this.serverAddresses[Math.floorMod(serverCycle.getAndIncrement(), this.serverAddresses.length)];
        return address.endsWith("/") ? address.substring(0, address.length() - 1) : address;
    }

}
