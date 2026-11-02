package xyz.erupt.ai_dev.runner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Cursor CLI: {@code cursor-agent -p --output-format stream-json --force}; {@code --force} lets print mode
 * write files instead of only proposing edits. The prompt is the last argument. Its MCP servers come from
 * Cursor's own configuration, so progress and verdicts come from the marker lines.
 *
 * @author YuePeng
 * date 2026/10/23
 */
@Component
public class CursorRunner extends StreamJsonRunner {

    @Override
    public String code() {
        return "cursor";
    }

    @Override
    protected String defaultBinary() {
        return "cursor-agent";
    }

    @Override
    protected String defaultLabel() {
        return "Cursor CLI";
    }

    @Override
    protected List<String> defaultArgs() {
        return List.of("--force");
    }

    @Override
    public boolean promptOnStdin() {
        return false;
    }

    @Override
    public List<String> command(Spec spec) {
        List<String> command = new ArrayList<>(List.of(this.binary(), "-p", "--output-format", "stream-json"));
        command.addAll(this.args());
        if (StringUtils.isNotBlank(spec.model())) command.addAll(List.of("--model", CodingAgentRunner.safeArg(spec.model())));
        if (StringUtils.isNotBlank(spec.resumeSessionId())) command.addAll(List.of("--resume", CodingAgentRunner.safeArg(spec.resumeSessionId())));
        command.add(spec.prompt());
        return command;
    }

}
