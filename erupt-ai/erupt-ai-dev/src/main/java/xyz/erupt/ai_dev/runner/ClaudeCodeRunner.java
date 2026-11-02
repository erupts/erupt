package xyz.erupt.ai_dev.runner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.mcp.DevMcpGrants;

import java.util.ArrayList;
import java.util.List;

/**
 * Claude Code in headless mode: {@code claude -p --output-format stream-json --verbose}, the prompt on
 * stdin; its output is the stream-json {@link StreamJsonRunner} reads.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Component
public class ClaudeCodeRunner extends StreamJsonRunner {

    public static final String CODE = "claude";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    protected String defaultBinary() {
        return "claude";
    }

    @Override
    protected String defaultLabel() {
        return "Claude Code";
    }

    @Override
    protected List<String> defaultArgs() {
        return List.of("--permission-mode", "acceptEdits", "--allowedTools", "Read,Edit,MultiEdit,Write,Glob,Grep,Bash");
    }

    // the aliases Claude Code resolves to the latest model of each family
    @Override
    public List<String> knownModels() {
        return List.of("opus", "sonnet", "haiku", "fable");
    }

    @Override
    public List<String> command(Spec spec) {
        List<String> command = new ArrayList<>(List.of(this.binary(), "-p", "--output-format", "stream-json", "--verbose"));
        List<String> args = this.args();
        if (null != spec.mcpConfig()) {
            // only erupt's server for this run, none of the account's own, and its tools allowed
            command.addAll(List.of("--mcp-config", spec.mcpConfig().toString(), "--strict-mcp-config"));
            int allowed = Math.max(args.indexOf("--allowedTools"), args.indexOf("--allowed-tools"));
            String tools = "mcp__" + DevMcpGrants.SERVER;
            if (allowed >= 0 && allowed + 1 < args.size()) args.set(allowed + 1, args.get(allowed + 1) + "," + tools);
            else args.addAll(List.of("--allowedTools", tools));
        }
        command.addAll(args);
        if (StringUtils.isNotBlank(spec.model())) command.addAll(List.of("--model", CodingAgentRunner.safeArg(spec.model())));
        if (StringUtils.isNotBlank(spec.resumeSessionId())) command.addAll(List.of("--resume", CodingAgentRunner.safeArg(spec.resumeSessionId())));
        return command;
    }

}
