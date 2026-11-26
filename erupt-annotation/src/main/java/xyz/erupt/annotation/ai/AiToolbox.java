package xyz.erupt.annotation.ai;

import java.lang.annotation.*;

/**
 * Marks a Spring bean whose {@code @Tool} methods are AI-callable.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface AiToolbox {

    /**
     * Whether the tools are offered to chat agents, role settings and user MCP tokens. false keeps them for
     * the grants of an McpAuthenticator, e.g. a coding agent run that reports its own progress.
     */
    boolean exposed() default true;

    /**
     * Whether the tools act on what the whole deployment shares — the server, its configuration, or tables
     * no tenant owns (knowledge bases, skills, decisions, A2A agents). Such tools are granted to platform
     * users only; a tenant user, its administrator included, is never handed them.
     */
    boolean platform() default false;

}
