package xyz.erupt.cloud.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2022/1/28
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt.cloud-server")
public class EruptCloudServerProp {

    // Node persistence duration
    private Integer nodeExpireTime = 1000 * 60 * 10;

    // Node node survival check cycle
    private Integer nodeSurviveCheckTime = 1000 * 120;

    // Cloud Key Namespace
    private String cloudNameSpace = "erupt-cloud:";

    // Does the node registration process verify the Access Token?
    private boolean validateAccessToken = true;

    // Node node request timeout period
    private Integer nodeRequestTimeout = 1000 * 30;

    // Also send the access token itself next to the request signature. Nodes released before signatures only
    // know the token; turn this off once every node verifies Erupt-Signature, so the token never travels
    private boolean plainAccessToken = true;

}
