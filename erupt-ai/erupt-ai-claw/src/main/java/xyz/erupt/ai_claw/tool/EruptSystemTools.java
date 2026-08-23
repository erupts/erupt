package xyz.erupt.ai_claw.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_claw.prop.EruptAiClawProp;
import xyz.erupt.ai_claw.util.ClawSandbox;
import xyz.erupt.annotation.ai.AiToolbox;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Controlled shell execution. Guardrails, in evaluation order:
 * 1. A hardline deny list blocks catastrophic commands regardless of configuration.
 * 2. The working directory is anchored to the per-user sandbox (~/.erupt/{account});
 *    other directories must be allowlisted via erupt.ai.claw.shell-allowed-paths.
 * 3. Credential-looking environment variables are stripped from the child process.
 * 4. Output is truncated so a single command can never flood the model context.
 *
 * @author YuePeng
 * date 2026/8/20
 */
@AiToolbox
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class EruptSystemTools {

    @Resource
    private EruptAiClawProp eruptAiClawProp;

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    private static final int MAX_OUTPUT_CHARS = 64 * 1024;

    private static final int MAX_TIMEOUT_SECONDS = 600;

    // Blocked even when shell execution is enabled; not configurable by design
    private static final Pattern[] HARDLINE_DENY = {
            Pattern.compile("rm\\s+(-[a-zA-Z]+\\s+)*(/|/\\*)(\\s|$)"),
            Pattern.compile("--no-preserve-root"),
            Pattern.compile("\\bmkfs"),
            Pattern.compile(":\\(\\)\\s*\\{"), // fork bomb
            Pattern.compile("\\b(shutdown|reboot|poweroff|halt)\\b"),
            Pattern.compile("\\bdd\\b[^|;]*\\bof=/dev/"),
            Pattern.compile(">\\s*/dev/(sd|nvme|disk)")
    };

    // Environment variables whose names look credential-bearing are not inherited
    private static final Pattern SENSITIVE_ENV = Pattern.compile("KEY|TOKEN|SECRET|PASSWORD|PASSWD|CREDENTIAL",
            Pattern.CASE_INSENSITIVE);

    @Tool("Execute a shell command on the server. On Windows uses cmd /c, on Unix uses bash -c. " +
            "Runs inside the current user's file sandbox by default; other working directories must be " +
            "allowlisted by the administrator. Prefer read-only commands. " +
            "Avoid destructive operations unless explicitly confirmed by the user.")
    public String execShell(
            @P("Shell command to execute.") String command,
            @P("Working directory. Relative paths resolve against the sandbox root; empty means the sandbox root.") String workdir,
            @P("Timeout in seconds before the command is forcibly terminated. Default is 30, max is 600.") int timeoutSeconds) {
        if (!eruptAiClawProp.isEnableExecShell()) {
            return "Shell execution is disabled";
        }
        if (null == command || command.isBlank()) return "Error: command is empty";
        for (Pattern pattern : HARDLINE_DENY) {
            if (pattern.matcher(command).find()) {
                return "Blocked: command matches a non-configurable deny rule: " + pattern.pattern();
            }
        }
        try {
            ProcessBuilder pb = IS_WINDOWS
                    ? new ProcessBuilder("cmd", "/c", command)
                    : new ProcessBuilder("bash", "-c", command);
            pb.redirectErrorStream(true);
            pb.directory(resolveWorkdir(workdir).toFile());
            pb.environment().keySet().removeIf(name -> SENSITIVE_ENV.matcher(name).find());
            int timeout = Math.min(timeoutSeconds <= 0 ? 30 : timeoutSeconds, MAX_TIMEOUT_SECONDS);
            Process process = pb.start();
            boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
            String output = truncate(new String(process.getInputStream().readAllBytes()));
            if (!finished) {
                process.destroyForcibly();
                return "Error: Command timed out after " + timeout + "s. Partial output:\n" + output;
            }
            return "Exit code: " + process.exitValue() + "\n" + output;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // Anchor the working directory to the sandbox or an administrator-allowlisted path
    private Path resolveWorkdir(String workdir) {
        Path sandbox = ClawSandbox.root();
        if (null == workdir || workdir.isBlank()) return sandbox;
        Path dir = Paths.get(workdir.trim());
        if (!dir.isAbsolute()) dir = sandbox.resolve(dir);
        dir = dir.normalize();
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Working directory not found: " + workdir);
        }
        try {
            Path real = dir.toRealPath();
            if (real.startsWith(sandbox.toRealPath())) return dir;
            for (String allowed : eruptAiClawProp.getShellAllowedPaths()) {
                if (real.startsWith(Paths.get(allowed).toAbsolutePath().normalize())) return dir;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot resolve working directory: " + e.getMessage());
        }
        throw new IllegalArgumentException("Working directory is outside the sandbox. " +
                "Ask the administrator to allow it via erupt.ai.claw.shell-allowed-paths: " + workdir);
    }

    private String truncate(String output) {
        if (output.length() <= MAX_OUTPUT_CHARS) return output;
        return output.substring(0, MAX_OUTPUT_CHARS) + "\n... [output truncated at " + (MAX_OUTPUT_CHARS / 1024) + "KB]";
    }

}
