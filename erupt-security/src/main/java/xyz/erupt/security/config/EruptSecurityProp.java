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

    // 是否记录操作日志
    private boolean recordOperateLog = true;
}
