package xyz.erupt.ai_dev.runner;

import org.apache.commons.lang3.StringUtils;
import xyz.erupt.ai_dev.constant.EventKind;
import xyz.erupt.ai_dev.prop.EruptAiDevProp;

import java.util.ArrayList;
import java.util.List;

/**
 * Any other CLI, added under {@code erupt.ai-dev.executor.clis}: it gets the prompt (on stdin, or as its
 * last argument) and prints text, each line kept as the agent's output. It succeeds when it exits with 0.
 * It has no session to resume and no MCP: progress and verdicts come from the marker lines.
 *
 * @author YuePeng
 * date 2026/10/23
 */
public class PlainCliRunner implements CodingAgentRunner {

    private final String code;

    private final EruptAiDevProp.Cli cli;

    public PlainCliRunner(String code, EruptAiDevProp.Cli cli) {
        this.code = code;
        this.cli = cli;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String label() {
        return StringUtils.defaultIfBlank(cli.getLabel(), code);
    }

    @Override
    public String binary() {
        return StringUtils.defaultIfBlank(cli.getBinary(), code);
    }

    @Override
    public boolean promptOnStdin() {
        return !cli.isPromptAsArgument();
    }

    @Override
    public boolean supportsResume() {
        return false;
    }

    @Override
    public List<String> command(Spec spec) {
        List<String> command = new ArrayList<>();
        command.add(this.binary());
        if (null != cli.getArgs()) command.addAll(cli.getArgs());
        if (!this.promptOnStdin()) command.add(spec.prompt());
        return command;
    }

    @Override
    public void onLine(String line, Listener listener) {
        if (StringUtils.isNotBlank(line)) listener.event(EventKind.TEXT, null, line);
    }

}
