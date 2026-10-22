package xyz.erupt.ai_dev.mcp;

import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import xyz.erupt.ai_dev.constant.AgentRole;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bearer tokens of the agent runs in flight, accepted by erupt-ai's MCP endpoint ({@link DevMcpAuthenticator}). A token is minted for one
 * run and revoked when it ends; it lists and calls the {@link DevTools} of its role and nothing else, and they
 * reach only that run's requirement.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Component
public class DevMcpGrants {

    // the agent's name for erupt's MCP server: its tools show up as mcp__erupt__<tool>
    public static final String SERVER = "erupt";

    // the MCP endpoint comes with erupt-ai, an optional dependency
    private static final boolean SERVED = ClassUtils.isPresent("xyz.erupt.ai.mcp.McpAuthenticator", DevMcpGrants.class.getClassLoader());

    static final Set<String> DEVELOPER_TOOLS = Set.of(DevTools.REQUIREMENT, DevTools.REPORT_PROGRESS);

    static final Set<String> QA_TOOLS = Set.of(DevTools.REQUIREMENT, DevTools.REPORT_VERDICT);

    /**
     * What a token may act on. The QA verdict it reports is kept here for the executor to collect.
     */
    public static final class Grant {

        private final String tenant;

        private final Long projectId;

        private final Long requirementId;

        private final Long sessionRow;

        private final AgentRole role;

        private volatile String verdict;

        Grant(String tenant, Long projectId, Long requirementId, Long sessionRow, AgentRole role) {
            this.tenant = tenant;
            this.projectId = projectId;
            this.requirementId = requirementId;
            this.sessionRow = sessionRow;
            this.role = role;
        }

        public String tenant() {
            return tenant;
        }

        public Long projectId() {
            return projectId;
        }

        public Long requirementId() {
            return requirementId;
        }

        public Long sessionRow() {
            return sessionRow;
        }

        public AgentRole role() {
            return role;
        }

        public String verdict() {
            return verdict;
        }

        void verdict(String verdict) {
            this.verdict = verdict;
        }
    }

    private final SecureRandom random = new SecureRandom();

    private final Map<String, Grant> grants = new ConcurrentHashMap<>();

    public String issue(String tenant, Long projectId, Long requirementId, Long sessionRow, AgentRole role) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        grants.put(token, new Grant(tenant, projectId, requirementId, sessionRow, role));
        return token;
    }

    /**
     * Whether erupt serves the MCP endpoint at all; without erupt-ai the agents report through marker lines.
     */
    public boolean served() {
        return SERVED;
    }

    public Grant get(String token) {
        return null == token ? null : grants.get(token);
    }

    /**
     * Ends the token; answers its grant for whatever the run reported.
     */
    public Grant revoke(String token) {
        return grants.remove(token);
    }

}
