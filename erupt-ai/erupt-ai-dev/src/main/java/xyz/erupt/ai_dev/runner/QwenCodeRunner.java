package xyz.erupt.ai_dev.runner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Qwen Code: {@code qwen --output-format stream-json --yolo}, the prompt on stdin (a non-terminal stdin
 * runs it headless). {@code --yolo} approves every tool, since headless runs cannot be asked. Its MCP
 * servers come from its own settings, so progress and verdicts come from the marker lines.
 *
 * @author YuePeng
 * date 2026/10/23
 */
@Component
public class QwenCodeRunner extends StreamJsonRunner {

    @Override
    public String code() {
        return "qwen";
    }

    @Override
    protected String defaultBinary() {
        return "qwen";
    }

    @Override
    protected String defaultLabel() {
        return "Qwen Code";
    }

    @Override
    protected List<String> defaultArgs() {
        return List.of("--yolo");
    }

    @Override
    public List<String> command(Spec spec) {
        List<String> command = new ArrayList<>(List.of(this.binary(), "--output-format", "stream-json"));
        command.addAll(this.args());
        if (StringUtils.isNotBlank(spec.model())) command.addAll(List.of("-m", CodingAgentRunner.safeArg(spec.model())));
        if (StringUtils.isNotBlank(spec.resumeSessionId())) command.addAll(List.of("--resume", CodingAgentRunner.safeArg(spec.resumeSessionId())));
        return command;
    }

}
