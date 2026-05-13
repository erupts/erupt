package xyz.erupt.ai_claw.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author YuePeng
 * date 2025/2/25 22:19
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt.ai.claw")
public class EruptAiClawProp {

    private boolean enabled = false;

    private boolean enableExecShell = true;

}
