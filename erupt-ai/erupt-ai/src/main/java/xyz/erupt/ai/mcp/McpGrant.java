package xyz.erupt.ai.mcp;

import java.util.Set;

/**
 * What an MCP token issued by an {@link McpAuthenticator} may do.
 *
 * @param uid        user the calls run as, null for a token that acts for no user
 * @param tools      names of the tools the token may list and call
 * @param attachment the issuer's own context, read by its tools through {@link McpContext}
 * @author YuePeng
 * date 2026/10/22
 */
public record McpGrant(Long uid, Set<String> tools, Object attachment) {
}
