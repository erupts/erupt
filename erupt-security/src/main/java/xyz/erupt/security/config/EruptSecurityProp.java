package xyz.erupt.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2020/12/5 15:43
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "erupt.security", ignoreUnknownFields = false)
public class EruptSecurityProp {

    // Whether to record operation logs
    private boolean recordOperateLog = true;

    // Max JSON request body size (bytes) buffered for operation logging; larger or chunked bodies are passed through without buffering
    private long recordOperateLogMaxBodySize = 1024 * 1024;
}
