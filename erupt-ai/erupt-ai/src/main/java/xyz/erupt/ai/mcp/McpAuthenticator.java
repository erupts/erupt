package xyz.erupt.ai.mcp;

/**
 * Lets a module accept its own bearer tokens on erupt's MCP endpoint, alongside the open-api secrets of
 * users. A grant names exactly the tools the token may list and call, exposed or not, and carries
 * whatever the tools need to know about the caller.
 *
 * @author YuePeng
 * date 2026/10/22
 */
public interface McpAuthenticator {

    /**
     * The grant of a token, or null when the token is not this authenticator's.
     */
    McpGrant authenticate(String token);

}
