package xyz.erupt.ai_claw.skill;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Shared storage layer for the skill library (~/.erupt/skills).
 * Skills are plain directories following the Agent Skills open standard;
 * per-skill metadata lives in a .usage.json file next to SKILL.md so a skill
 * directory stays self-contained and portable. Usage tracking is best-effort:
 * failures are logged but never break the calling tool.
 *
 * @author YuePeng
 * date 2026/8/20
 */
@Slf4j
public class SkillStore {

    public static final String SKILL_MD = "SKILL.md";

    public static final String ARCHIVE_DIR = ".archive";

    public static final String USAGE_FILE = ".usage.json";

    public static final String LEDGER_FILE = ".curator_ledger.jsonl";

    private SkillStore() {
    }

    // Skill library root shared by all users, created on first access
    public static Path root() {
        try {
            Path root = Paths.get(System.getProperty("user.home"), EruptConst.ERUPT_DIR, "skills")
                    .toAbsolutePath().normalize();
            Files.createDirectories(root);
            return root;
        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize skill directory: " + e.getMessage(), e);
        }
    }

    // Names of all live (non-archived) skill directories that contain a SKILL.md
    public static Set<String> skillNames() {
        try (Stream<Path> dirs = Files.list(root())) {
            return dirs.filter(Files::isDirectory)
                    .filter(dir -> !dir.getFileName().toString().startsWith("."))
                    .filter(dir -> Files.exists(dir.resolve(SKILL_MD)))
                    .map(dir -> dir.getFileName().toString())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("Cannot list skills: {}", e.getMessage());
            return Set.of();
        }
    }

    @Getter
    @Setter
    public static class Usage {
        private int uses;
        private int patches;
        private String lastUsed;
    }

    public static Usage readUsage(Path skillDir) {
        Path usageFile = skillDir.resolve(USAGE_FILE);
        try {
            if (Files.exists(usageFile)) {
                return GsonFactory.getGson().fromJson(Files.readString(usageFile), Usage.class);
            }
        } catch (Exception e) {
            log.warn("Unreadable skill usage file, resetting: {}", usageFile);
        }
        return new Usage();
    }

    public static void recordUse(Path skillDir) {
        Usage usage = readUsage(skillDir);
        usage.setUses(usage.getUses() + 1);
        touch(skillDir, usage);
    }

    public static void recordPatch(Path skillDir) {
        Usage usage = readUsage(skillDir);
        usage.setPatches(usage.getPatches() + 1);
        touch(skillDir, usage);
    }

    private static void touch(Path skillDir, Usage usage) {
        usage.setLastUsed(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        try {
            Files.writeString(skillDir.resolve(USAGE_FILE), GsonFactory.getGson().toJson(usage));
        } catch (Exception e) {
            log.warn("Cannot write skill usage file in {}: {}", skillDir, e.getMessage());
        }
    }

    // Parse the "description:" value from the SKILL.md YAML frontmatter; null when absent
    public static String readDescription(Path skillDir) {
        try {
            boolean inFrontmatter = false;
            for (String line : Files.readAllLines(skillDir.resolve(SKILL_MD))) {
                if (line.trim().equals("---")) {
                    if (inFrontmatter) break;
                    inFrontmatter = true;
                    continue;
                }
                if (inFrontmatter && line.trim().toLowerCase(Locale.ROOT).startsWith("description:")) {
                    return line.trim().substring("description:".length()).trim();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // A skill is pinned via SKILL.md frontmatter "pinned: true"; pinned skills are exempt from curation
    public static boolean isPinned(Path skillDir) {
        try {
            boolean inFrontmatter = false;
            for (String line : Files.readAllLines(skillDir.resolve(SKILL_MD))) {
                if (line.trim().equals("---")) {
                    if (inFrontmatter) break;
                    inFrontmatter = true;
                    continue;
                }
                if (inFrontmatter && line.replace(" ", "").equalsIgnoreCase("pinned:true")) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    // Append-only curator audit trail; one JSON object per line
    public static void ledger(String action, String skill, String reason) {
        JsonObject entry = new JsonObject();
        entry.addProperty("time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        entry.addProperty("action", action);
        entry.addProperty("skill", skill);
        entry.addProperty("reason", reason);
        try {
            Files.writeString(root().resolve(LEDGER_FILE), entry + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            log.warn("Cannot append curator ledger: {}", e.getMessage());
        }
    }

}
