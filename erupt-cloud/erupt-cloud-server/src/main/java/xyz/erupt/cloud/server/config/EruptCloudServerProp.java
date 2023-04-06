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

    // node 节点持久化时长
    private Integer nodeExpireTime = 1000 * 60 * 10;

    // node 节点存活检查周期
    private Integer nodeSurviveCheckTime = 1000 * 120;

    // cloud key 命名空间
    private String cloudNameSpace = "erupt-cloud:";

    // node 节点注册时是否校验 Access Token
    private boolean validateAccessToken = true;

}
