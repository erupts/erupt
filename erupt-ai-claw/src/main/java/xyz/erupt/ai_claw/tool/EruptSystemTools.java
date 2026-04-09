package xyz.erupt.ai_claw.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.annotation.AiToolbox;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

@AiToolbox
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class EruptSystemTools {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    @Resource
    private Environment environment;

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

    @Tool("Get currently active Spring profiles (e.g. dev, prod, staging). " +
            "Helps understand the current runtime environment before taking any action.")
    public String getActiveProfiles() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length == 0 ? "No active profiles (using default)" : String.join(", ", profiles);
    }

    @Tool("Get a Spring application configuration property value by key (e.g. spring.datasource.url). " +
            "Reads from application.yml/properties and all active Spring Environment sources. " +
            "Returns the resolved runtime value, not the raw file content.")
    public String getProperty(
            @P("Spring property key, e.g. spring.datasource.url or erupt.ai.model") String key) {
        String value = environment.getProperty(key);
        return value != null ? key + " = " + value : "Property not found: " + key;
    }

    @Tool("Get JVM heap memory usage of the current process: used, total, and max heap in MB.")
    public String getJvmMemory() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        long total = rt.totalMemory() / 1024 / 1024;
        long max = rt.maxMemory() / 1024 / 1024;
        return "Heap used: " + used + "MB / total: " + total + "MB / max: " + max + "MB";
    }

    @Tool("Get JVM thread statistics: total thread count and deadlocked thread IDs if any. " +
            "Useful for diagnosing hangs or thread exhaustion.")
    public String getThreadInfo() {
        var bean = ManagementFactory.getThreadMXBean();
        long[] deadlocked = bean.findDeadlockedThreads();
        String deadlockInfo = deadlocked == null ? "none" : deadlocked.length + " deadlocked thread(s) detected!";
        return "Thread count: " + bean.getThreadCount()
                + "\nPeak thread count: " + bean.getPeakThreadCount()
                + "\nDeadlocks: " + deadlockInfo;
    }

}
