package xyz.erupt.ai_dev.mcp;

import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.mcp.McpAuthenticator;
import xyz.erupt.ai.mcp.McpGrant;
import xyz.erupt.ai_dev.constant.AgentRole;

/**
 * Lets a run's token in on erupt-ai's MCP endpoint, with the {@link DevTools} of its role. Only wired when
 * erupt-ai is on the classpath.
 *
 * @author YuePeng
 * date 2026/10/23
 */
@Component
@ConditionalOnClass(name = "xyz.erupt.ai.mcp.McpAuthenticator")
public class DevMcpAuthenticator implements McpAuthenticator {

    @Resource
    private DevMcpGrants devMcpGrants;

    @Override
    public McpGrant authenticate(String token) {
        DevMcpGrants.Grant grant = devMcpGrants.get(token);
        if (null == grant) return null;
        return new McpGrant(null, grant.role() == AgentRole.QA ? DevMcpGrants.QA_TOOLS : DevMcpGrants.DEVELOPER_TOOLS, grant);
    }

}
