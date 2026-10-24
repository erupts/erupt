package xyz.erupt.ai_dev.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * erupt:
 *   ai-dev:
 *     root: /data/erupt-ai-dev
 *     credentials:
 *       github:
 *         token: ${GITHUB_TOKEN}
 *         hosts: [github.com]
 *       internal:
 *         username: deploy
 *         password: ${GIT_INTERNAL_PASSWORD}
 *         hosts: [git.internal.example.com]
 *       gitlab-ssh:
 *         ssh-key: /etc/erupt/keys/gitlab
 *         hosts: ["*.gitlab.example.com"]
 * </pre>
 * A repository names its entry by key; secrets stay in the configuration, never in the database. A credential
 * goes only to the hosts it lists, so a repository URL typed into a project cannot send it anywhere else.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Getter
@Setter
@Component
@ConfigurationProperties("erupt.ai-dev")
public class EruptAiDevProp {

    // Holds repos/<project>/<alias>.git and worktrees/<project>/<requirement>/<alias>; blank = ~/.erupt/ai-dev
    private String root;

    private Map<String, Credential> credentials = new LinkedHashMap<>();

    private Executor executor = new Executor();

    /**
     * Runs coding agents on this server. Off by default: an agent edits files and runs commands with the
     * server's account, so turn it on only on a host meant for it (ideally a container or a VM of its own),
     * and on one instance only.
     */
    @Getter
    @Setter
    public static class Executor {

        private boolean enabled = false;

        // agent runs at the same time on this instance
        private int maxConcurrent = 2;

        // how often the queue is checked for approved requirements
        private int pollSeconds = 10;

        // a run still going after this long is killed
        private int runTimeoutMinutes = 60;

        // Where the agent reaches erupt's MCP endpoint; blank = http://127.0.0.1:<port><context-path>/mcp (erupt-ai).
        // Set it when the agent runs in a container that sees the server under another address
        private String mcpUrl;

        // The agent process starts from a clean environment (PATH, HOME, LANG only) plus these, so no
        // secret of the server (database password, tokens) leaks into it. Put the agent's own key here,
        // e.g. ANTHROPIC_API_KEY: ${ANTHROPIC_API_KEY}
        private Map<String, String> env = new LinkedHashMap<>();

        /**
         * Coding CLIs by executor code. A built-in (claude, codex, opencode, kimi) takes its binary and
         * extra arguments from here when set; any other key adds a plain CLI that gets the prompt on stdin
         * and answers in text, e.g. {@code zcode: {binary: zcode, args: [-p]}}.
         */
        private Map<String, Cli> clis = new LinkedHashMap<>();

        @Override
        public String toString() {
            return "Executor[enabled=" + enabled + "]";
        }
    }

    @Getter
    @Setter
    public static class Cli {

        // executable, from PATH unless absolute; blank keeps the built-in's own name
        private String binary;

        // shown in the executor picker; blank uses the built-in's label or the key
        private String label;

        // image URL shown for this CLI; blank draws its default badge (its initials in its color)
        private String icon;

        // Extra arguments, replacing the built-in's defaults when set. Headless runs cannot ask for
        // permission, so this is where the tools a run may use are granted
        private List<String> args;

        // where a plain CLI reads the prompt: stdin, or the last argument (visible in the process list)
        private boolean promptAsArgument = false;
    }

    @Getter
    @Setter
    public static class Credential {

        private String token;

        private String username;

        private String password;

        private String sshKey;

        // Hosts this credential may be sent to: exact names, "*.example.com" for subdomains, or "*" for any
        // host. None listed means it is never used, so a project cannot point it at a server of its own
        private List<String> hosts = new ArrayList<>();

        // Never print the secret
        @Override
        public String toString() {
            return "Credential[***]";
        }
    }

}
