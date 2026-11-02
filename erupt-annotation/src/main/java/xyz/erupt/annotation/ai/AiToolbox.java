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

}
