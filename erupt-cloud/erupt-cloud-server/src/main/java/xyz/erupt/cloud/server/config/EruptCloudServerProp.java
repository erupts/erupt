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

}
