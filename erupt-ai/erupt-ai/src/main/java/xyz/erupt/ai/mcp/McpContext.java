package xyz.erupt.ai.mcp;

import java.util.function.Supplier;

/**
 * The grant of the MCP call being served on this thread, set by the MCP endpoint for the duration of
 * one call.
 *
 * @author YuePeng
 * date 2026/10/22
 */
public final class McpContext {

    private static final ThreadLocal<McpGrant> GRANT = new ThreadLocal<>();

    private McpContext() {
    }

    public static McpGrant grant() {
        return GRANT.get();
    }

    /**
     * The attachment of the current grant when it is of the given type, otherwise null: a tool called
     * from chat or by another issuer's token finds nothing.
     */
    public static <T> T attachment(Class<T> type) {
        McpGrant grant = GRANT.get();
        return null != grant && type.isInstance(grant.attachment()) ? type.cast(grant.attachment()) : null;
    }

    /**
     * Runs one MCP call under a grant.
     */
    public static <T> T call(McpGrant grant, Supplier<T> body) {
        GRANT.set(grant);
        try {
            return body.get();
        } finally {
            GRANT.remove();
        }
    }

}
