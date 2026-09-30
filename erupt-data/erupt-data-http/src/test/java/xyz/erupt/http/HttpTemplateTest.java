package xyz.erupt.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Template resolution outside a Spring context: SpEL against the root works, property
 * placeholders and bean references are left for the runtime.
 *
 * @author YuePeng
 */
public class HttpTemplateTest {

    @Test
    void plainStringsPassThrough() {
        assertEquals("https://api.example.com/users", HttpTemplate.resolve("https://api.example.com/users"));
        assertEquals("Bearer ${gh.token}", HttpTemplate.resolve("Bearer ${gh.token}"));
    }

    @Test
    void spelEvaluatesAgainstRoot() {
        // no session here, so user is null and the safe navigation yields an empty segment
        assertEquals("https://api/tenants//users", HttpTemplate.resolve("https://api/tenants/#{user?.tenantId ?: ''}/users"));
        assertEquals("v3", HttpTemplate.resolve("v#{1 + 2}"));
    }

}
