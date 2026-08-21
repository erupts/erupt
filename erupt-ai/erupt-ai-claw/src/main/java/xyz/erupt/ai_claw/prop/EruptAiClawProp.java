package xyz.erupt.ai_claw.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/2/25 22:19
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt.ai.claw")
public class EruptAiClawProp {

    private boolean enabled = true;

    private boolean enableExecShell = false;

    // Directories outside the per-user sandbox where execShell may run (absolute paths)
    private List<String> shellAllowedPaths = new ArrayList<>();

    // Daily background archiving of stale skills (moved to .archive, never deleted)
    private boolean skillCuratorEnabled = true;

    // A skill unused for this many days is considered stale
    private int skillStaleDays = 30;

}
