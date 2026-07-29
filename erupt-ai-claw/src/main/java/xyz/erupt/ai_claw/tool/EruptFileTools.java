package xyz.erupt.ai_claw.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.context.MetaContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * File operations confined to a per-user sandbox directory: ~/.erupt/{account}
 * (under the user's home directory, so it survives working-directory changes
 * and the framework's .erupt reset on data reload).
 * Every path argument is resolved against the sandbox root; escapes via ".." or
 * symlinks are rejected, so the model can never touch files outside the sandbox.
 *
 * @author YuePeng
 * date 2026/7/29
 */
@AiToolbox
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class EruptFileTools {

    private static final long MAX_READ_BYTES = 512 * 1024;

    @Tool("List files and directories inside the current user's file sandbox. " +
            "Pass an empty path to list the sandbox root. Returns name, type and size.")
    public String listFiles(@P("Directory path relative to the sandbox root, empty for root") String path) {
        try {
            Path dir = resolveInSandbox(path);
            if (!Files.exists(dir)) return "Directory not found: " + display(path);
            if (!Files.isDirectory(dir)) return "Not a directory: " + display(path);
            StringJoiner sj = new StringJoiner("\n");
            try (Stream<Path> stream = Files.list(dir)) {
                stream.sorted().forEach(p -> sj.add((Files.isDirectory(p) ? "[dir]  " : "[file] ")
                        + sandboxRoot().relativize(p) + (Files.isDirectory(p) ? "" : " (" + fileSize(p) + ")")));
            }
            return sj.length() == 0 ? "Empty directory: " + display(path) : sj.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Read a text file from the current user's file sandbox and return its content.")
    public String readFile(@P("File path relative to the sandbox root") String path) {
        try {
            Path file = resolveInSandbox(path);
            if (!Files.exists(file)) return "File not found: " + display(path);
            if (Files.isDirectory(file)) return "Path is a directory, use listFiles instead: " + display(path);
            if (Files.size(file) > MAX_READ_BYTES) {
                return "File too large to read (" + fileSize(file) + "), limit is " + (MAX_READ_BYTES / 1024) + "KB";
            }
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Write a text file inside the current user's file sandbox. " +
            "Creates parent directories automatically and overwrites the file if it already exists.")
    public String writeFile(@P("File path relative to the sandbox root") String path,
                            @P("Text content to write") String content) {
        try {
            Path file = resolveInSandbox(path);
            if (Files.isDirectory(file)) return "Path is a directory: " + display(path);
            if (null != file.getParent()) Files.createDirectories(file.getParent());
            Files.writeString(file, null == content ? "" : content, StandardCharsets.UTF_8);
            return "File written: " + display(path) + " (" + fileSize(file) + ")";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Append text to a file inside the current user's file sandbox, creating it if absent.")
    public String appendFile(@P("File path relative to the sandbox root") String path,
                             @P("Text content to append") String content) {
        try {
            Path file = resolveInSandbox(path);
            if (Files.isDirectory(file)) return "Path is a directory: " + display(path);
            if (null != file.getParent()) Files.createDirectories(file.getParent());
            Files.writeString(file, null == content ? "" : content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return "Content appended: " + display(path) + " (" + fileSize(file) + ")";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Delete a file or an empty directory inside the current user's file sandbox. " +
            "Non-empty directories are rejected — delete their contents first.")
    public String deleteFile(@P("Path relative to the sandbox root") String path) {
        try {
            Path file = resolveInSandbox(path);
            if (file.equals(sandboxRoot())) return "Cannot delete the sandbox root";
            if (!Files.exists(file)) return "File not found: " + display(path);
            if (Files.isDirectory(file)) {
                try (Stream<Path> stream = Files.list(file)) {
                    if (stream.findAny().isPresent()) return "Directory not empty: " + display(path);
                }
            }
            Files.delete(file);
            return "Deleted: " + display(path);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Move or rename a file within the current user's file sandbox.")
    public String moveFile(@P("Source path relative to the sandbox root") String source,
                           @P("Target path relative to the sandbox root") String target) {
        try {
            Path from = resolveInSandbox(source);
            Path to = resolveInSandbox(target);
            if (!Files.exists(from)) return "File not found: " + display(source);
            if (null != to.getParent()) Files.createDirectories(to.getParent());
            Files.move(from, to);
            return "Moved: " + display(source) + " -> " + display(target);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // Sandbox root of the current user, created on first access
    private Path sandboxRoot() {
        try {
            Path root = Paths.get(System.getProperty("user.home"), EruptConst.ERUPT_DIR, MetaContext.getUser().getAccount())
                    .toAbsolutePath().normalize();
            Files.createDirectories(root);
            return root;
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize sandbox: " + e.getMessage(), e);
        }
    }

    // Resolve a user-supplied path against the sandbox root, rejecting ".." and symlink escapes
    private Path resolveInSandbox(String path) throws IOException {
        Path root = sandboxRoot();
        Path target = root.resolve(null == path ? "" : path.trim()).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Path escapes the sandbox: " + path);
        }
        if (Files.exists(target) && !target.toRealPath().startsWith(root.toRealPath())) {
            throw new IllegalArgumentException("Path escapes the sandbox via symlink: " + path);
        }
        return target;
    }

    private String display(String path) {
        return null == path || path.isBlank() ? "/" : path;
    }

    private String fileSize(Path path) {
        try {
            long bytes = Files.size(path);
            if (bytes < 1024) return bytes + "B";
            if (bytes < 1024 * 1024) return (bytes / 1024) + "KB";
            return String.format("%.1fMB", bytes / 1024.0 / 1024.0);
        } catch (IOException e) {
            return "unknown";
        }
    }

}
