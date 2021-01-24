package xyz.erupt.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author liyuepeng
 * @date 2020/12/5 15:43
 */
@Data
@Component
@ConfigurationProperties(prefix = "erupt.security", ignoreUnknownFields = false)
public class EruptSecurityProp {

    // 是否记录操作日志
    private boolean recordOperateLog = true;
}
