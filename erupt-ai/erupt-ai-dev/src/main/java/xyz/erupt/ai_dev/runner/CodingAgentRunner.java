package xyz.erupt.ai_dev.runner;

import xyz.erupt.ai_dev.constant.EventKind;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * One coding CLI erupt can drive headless: how to start it for a run and how to read what it prints.
 * {@link DevRunners} holds the built-ins and the plain CLIs added by configuration.
 *
 * @author YuePeng
 * date 2026/10/22
 */
public interface CodingAgentRunner {

    // model ids and session ids go to the CLI as arguments: nothing option-like gets through
    Pattern SAFE_ARG = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:@/-]{0,127}");

    /**
     * Stored on the project's agent, lower case, e.g. claude.
     */
    String code();

    String label();

    /**
     * Image URL configured for the CLI, null for the page's default badge.
     */
    default String icon() {
        return null;
    }

    /**
     * Executable, from PATH unless absolute; whether it is found decides whether the executor is offered.
     */
    String binary();

    /**
     * The command line. The prompt goes to stdin unless {@link #promptOnStdin()} says otherwise, so it
     * stays out of the process list and has no length limit.
     */
    List<String> command(Spec spec);

    default boolean promptOnStdin() {
        return true;
    }

    /**
     * Model ids offered in the picker without asking the CLI; the field stays free to type any other.
     */
    default List<String> knownModels() {
        return List.of();
    }

    /**
     * A command printing one model id per line, for a CLI that can list its models; null when it cannot.
     */
    default List<String> listModelsCommand() {
        return null;
    }

    /**
     * Whether a retry can continue the CLI's previous session; without it the run starts over with the
     * whole requirement.
     */
    default boolean supportsResume() {
        return true;
    }

    /**
     * Variables the run adds to the agent's clean environment, e.g. the MCP token for a CLI that reads it
     * from there.
     */
    default Map<String, String> environment(Spec spec) {
        return Map.of();
    }

    /**
     * Reads one line of the CLI's output. A CLI that never reports a result is judged by its exit code,
     * with its last text as the answer.
     */
    void onLine(String line, Listener listener);

    /**
     * @param resumeSessionId the CLI session to continue, null for a new one
     * @param mcpConfig       MCP client file in the common {@code mcpServers} format, null when erupt's
     *                        endpoint is not reachable (the agent then reports through marker lines)
     * @param mcpUrl          the same endpoint for CLIs configured another way
     * @param mcpToken        the run's bearer token for it
     */
    record Spec(Path workspace, String prompt, String model, String resumeSessionId, Path mcpConfig,
                String mcpUrl, String mcpToken) {
    }

    interface Listener {

        void session(String sessionId);

        void event(EventKind kind, String name, String content);

        /**
         * The final answer; {@code success} is the agent's own verdict on its run.
         */
        void result(boolean success, String text, Double costUsd, Long tokens);
    }

    static String safeArg(String value) {
        if (null == value || !SAFE_ARG.matcher(value).matches()) throw new IllegalArgumentException("Unsafe argument: " + value);
        return value;
    }

}
