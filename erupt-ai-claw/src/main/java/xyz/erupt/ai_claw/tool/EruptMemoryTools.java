package xyz.erupt.ai_claw.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.annotation.AiToolbox;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YuePeng
 */
@AiToolbox
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class EruptMemoryTools {

    private static final String MEMORY_DIR = System.getProperty("user.home") + "/.erupt/memory/";

    @Tool("Save or overwrite a memory entry. Use a short descriptive key (e.g. 'user_preference', 'project_context'). " +
            "Memories persist across conversations and can be recalled at any time.")
    public String saveMemory(
            @P("Memory key — short identifier, no spaces, e.g. 'deploy_server'") String key,
            @P("Memory content to store") String content) {
        if (isUnsafeKey(key)) return "Error: invalid key.";
        try {
            Path dir = Path.of(MEMORY_DIR);
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(key + ".md"), content);
            return "Memory saved: " + key;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Retrieve a memory entry by key.")
    public String getMemory(@P("Memory key to retrieve") String key) {
        if (isUnsafeKey(key)) return "Error: invalid key.";
        try {
            Path file = Path.of(MEMORY_DIR, key + ".md");
            if (!Files.exists(file)) return "Memory not found: " + key;
            return Files.readString(file);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("List all saved memory keys with a one-line preview of each.")
    public String listMemories() {
        File dir = new File(MEMORY_DIR);
        if (!dir.exists() || dir.listFiles() == null) return "No memories saved yet.";
        List<String> result = new ArrayList<>();
        for (File f : dir.listFiles()) {
            if (!f.getName().endsWith(".md")) continue;
            String key = f.getName().replace(".md", "");
            String preview = readPreview(f);
            result.add(key + ": " + preview);
        }
        return result.isEmpty() ? "No memories saved yet." : String.join("\n", result);
    }

    @Tool("Delete a memory entry by key.")
    public String deleteMemory(@P("Memory key to delete") String key) {
        if (isUnsafeKey(key)) return "Error: invalid key.";
        File file = new File(MEMORY_DIR, key + ".md");
        if (!file.exists()) return "Memory not found: " + key;
        file.delete();
        return "Memory deleted: " + key;
    }

    private String readPreview(File f) {
        try {
            String first = Files.lines(f.toPath()).filter(l -> !l.isBlank()).findFirst().orElse("");
            return first.length() > 80 ? first.substring(0, 80) + "..." : first;
        } catch (Exception e) {
            return "(unreadable)";
        }
    }

    private boolean isUnsafeKey(String key) {
        return key == null || key.isBlank() || key.contains("..") || key.contains("/") || key.contains("\\");
    }

}
