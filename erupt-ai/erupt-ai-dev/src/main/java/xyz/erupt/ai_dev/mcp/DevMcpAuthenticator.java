package xyz.erupt.ai_dev.mcp;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.mcp.McpAuthenticator;
import xyz.erupt.ai.mcp.McpGrant;
import xyz.erupt.ai.service.LLMRoleService;
import xyz.erupt.ai_dev.constant.AgentRole;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Lets a run's token in on erupt-ai's MCP endpoint: the {@link DevTools} of its role, and erupt's own tools as
 * the requirement's creator, as far as their AI roles allow (what they could use in AI chat).
 *
 * @author YuePeng
 * date 2026/10/23
 */
@Component
public class DevMcpAuthenticator implements McpAuthenticator {

    @Resource
    private DevMcpGrants devMcpGrants;

    @Resource
    private LLMRoleService llmRoleService;

    @Override
    public McpGrant authenticate(String token) {
        DevMcpGrants.Grant grant = devMcpGrants.get(token);
        if (null == grant) return null;
        // the run's own tools, plus every erupt tool the requirement's creator may use in AI chat
        Set<String> tools = new LinkedHashSet<>(grant.role() == AgentRole.QA ? DevMcpGrants.QA_TOOLS : DevMcpGrants.DEVELOPER_TOOLS);
        if (null != grant.uid()) tools.addAll(llmRoleService.getAllowedToolsByUid(grant.uid()));
        return new McpGrant(grant.uid(), tools, grant);
    }

}
