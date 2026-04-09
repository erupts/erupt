package xyz.erupt.ai_claw.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.ai.AiToolbox;

import java.io.File;
import java.util.concurrent.TimeUnit;

@AiToolbox
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class EruptSystemTools {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    @Tool("Execute a shell command on the server. On Windows uses cmd /c, on Unix uses bash -c. " +
            "Use with caution — prefer read-only commands. Avoid destructive operations unless explicitly confirmed by the user.")
    public String execShell(
            @P("Shell command to execute.") String command,
            @P("Working directory for the command. Defaults to user home if not specified.") String workdir,
            @P("Timeout in seconds before the command is forcibly terminated. Default is 30.") int timeoutSeconds) {
        try {
            ProcessBuilder pb = IS_WINDOWS
                    ? new ProcessBuilder("cmd", "/c", command)
                    : new ProcessBuilder("bash", "-c", command);
            pb.redirectErrorStream(true);
            if (workdir != null && !workdir.isBlank()) {
                pb.directory(new File(workdir));
            }
            Process process = pb.start();
            boolean finished = process.waitFor(timeoutSeconds <= 0 ? 30 : timeoutSeconds, TimeUnit.SECONDS);
            String output = new String(process.getInputStream().readAllBytes());
            if (!finished) {
                process.destroyForcibly();
                return "Error: Command timed out after " + timeoutSeconds + "s. Partial output:\n" + output;
            }
            return "Exit code: " + process.exitValue() + "\n" + output;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

}
