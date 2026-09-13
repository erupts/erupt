package xyz.erupt.remote.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt.remote")
public class EruptRemoteProp {

    // AES key used to encrypt stored host credentials. Optional: when empty a random key is generated once into
    // .erupt/remote.key. Multi-node deployments must set the same value on every node (inject it via environment variable)
    private String secretKey;

    // maximum number of concurrent desktop sessions across the whole application
    private Integer maxSessions = 20;

    // sessions without any browser input for longer than this are closed
    private Integer idleTimeoutMinutes = 30;

    // TCP connect timeout towards the remote host
    private Integer connectTimeoutSeconds = 5;

}
