package xyz.erupt.ai_claw.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.annotation.AiToolbox;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@AiToolbox
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class EruptSystemTools {


    // ======================== Shell ========================
    @Tool("Execute a shell command on the server. Use with caution — prefer read-only commands. Avoid destructive operations unless explicitly confirmed by the user.")
    public String execShell(
            @P("Shell command to execute. Single command only; chaining with &&, ||, ; is not recommended.") String command,
            @P("Working directory for the command. Defaults to user home if not specified.") String workdir,
            @P("Timeout in seconds before the command is forcibly terminated. Default is 30.") int timeoutSeconds) {
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.redirectErrorStream(true);
            if (workdir != null && !workdir.isBlank()) {
                pb.directory(new File(workdir));
            }
            Process process = pb.start();
            boolean finished = process.waitFor(
                    timeoutSeconds <= 0 ? 30 : timeoutSeconds,
                    TimeUnit.SECONDS
            );
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

    // ======================== System Info ========================

    @Tool("Get current server system information including OS, CPU, memory, disk usage and uptime.")
    public String getSystemInfo() {
        Runtime rt = Runtime.getRuntime();
        long totalMemory = rt.totalMemory() / 1024 / 1024;
        long freeMemory = rt.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        return "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version")
                + "\nArch: " + System.getProperty("os.arch")
                + "\nCPU cores: " + rt.availableProcessors()
                + "\nJVM memory — used: " + usedMemory + "MB / total: " + totalMemory + "MB"
                + "\nUser home: " + System.getProperty("user.home")
                + "\nWorking dir: " + System.getProperty("user.dir")
                + "\nJava version: " + System.getProperty("java.version");
    }

    // ======================== File System ========================

    @Tool("Read the content of a file on the server. Suitable for config files, logs, and text files.")
    public String readFile(
            @P("Absolute file path to read") String path,
            @P("Maximum number of lines to read. 0 means read all. Use a limit for large files.") int maxLines) {
        try {
            Path filePath = Path.of(path);
            if (!Files.exists(filePath)) return "Error: file not found: " + path;
            if (Files.isDirectory(filePath)) return "Error: path is a directory, use listDir instead.";
            List<String> lines = Files.readAllLines(filePath);
            if (maxLines > 0 && lines.size() > maxLines) {
                lines = lines.subList(0, maxLines);
                return String.join("\n", lines) + "\n... (truncated at " + maxLines + " lines)";
            }
            return String.join("\n", lines);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("List files and directories at the given path.")
    public String listDir(
            @P("Absolute directory path to list") String path) {
        try {
            File dir = new File(path);
            if (!dir.exists()) return "Error: path not found: " + path;
            if (!dir.isDirectory()) return "Error: path is not a directory.";
            StringBuilder sb = new StringBuilder();
            for (File f : dir.listFiles()) {
                sb.append(f.isDirectory() ? "[DIR] " : "[FILE] ")
                        .append(f.getName())
                        .append(f.isFile() ? " (" + f.length() / 1024 + " KB)" : "")
                        .append("\n");
            }
            return sb.isEmpty() ? "(empty directory)" : sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Write content to a file on the server. Creates the file if it does not exist. Confirm with user before overwriting important files.")
    public String writeFile(
            @P("Absolute file path to write") String path,
            @P("Content to write into the file") String content,
            @P("true to append to existing file, false to overwrite") boolean append) {
        try {
            Path filePath = Path.of(path);
            Files.createDirectories(filePath.getParent());
            if (append) {
                Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                Files.writeString(filePath, content);
            }
            return "File written: " + path;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // ======================== Process ========================

    @Tool("List currently running JVM system properties and environment summary. Useful for diagnosing the application runtime environment.")
    public String getJvmInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== JVM Properties ===\n");
        System.getProperties().forEach((k, v) ->
                sb.append(k).append(" = ").append(v).append("\n"));
        return sb.toString();
    }

    @Tool("Get key environment variables of the current server process.")
    public String getEnvVars(
            @P("Comma-separated list of env var names to retrieve. Leave blank to get all (may be verbose).") String keys) {
        Map<String, String> env = System.getenv();
        if (keys != null && !keys.isBlank()) {
            StringBuilder sb = new StringBuilder();
            for (String key : keys.split(",")) {
                String k = key.trim();
                sb.append(k).append(" = ").append(env.getOrDefault(k, "(not set)")).append("\n");
            }
            return sb.toString();
        }
        return env.entrySet().stream()
                .map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.joining("\n"));
    }

}