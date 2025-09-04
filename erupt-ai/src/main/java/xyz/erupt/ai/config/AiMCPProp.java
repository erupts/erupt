package xyz.erupt.ai.config;

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
@ConfigurationProperties("erupt.ai.mcp")
public class AiMCPProp {

    private boolean enabled = false;

    private String name = "erupt-mcp";

    private String description;

}
