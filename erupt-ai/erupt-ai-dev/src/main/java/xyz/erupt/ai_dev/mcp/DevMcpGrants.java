package xyz.erupt.ai_dev.mcp;

import org.springframework.stereotype.Component;
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

        // the requirement's creator: erupt's own tools run as them, with what their AI roles allow
        private final Long uid;

        private volatile String verdict;

        Grant(String tenant, Long projectId, Long requirementId, Long sessionRow, AgentRole role, Long uid) {
            this.tenant = tenant;
            this.projectId = projectId;
            this.requirementId = requirementId;
            this.sessionRow = sessionRow;
            this.role = role;
            this.uid = uid;
        }

        public Long uid() {
            return uid;
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

    public String issue(String tenant, Long projectId, Long requirementId, Long sessionRow, AgentRole role, Long uid) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        grants.put(token, new Grant(tenant, projectId, requirementId, sessionRow, role, uid));
        return token;
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
