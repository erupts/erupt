package xyz.erupt.ai_claw.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_claw.skill.SkillStore;
import xyz.erupt.annotation.ai.AiToolbox;

import java.io.File;
import java.nio.file.Files;

/**
 * Tools that write the shared skill library. Kept apart from {@link EruptSkillTools} (reading) so a grant,
 * and the prompt section that goes with it, can be told apart by toolbox rather than by tool name.
 */
@AiToolbox(platform = true)
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class EruptSkillAuthorTools {

    private static final String SKILL_DIR = SkillStore.root() + File.separator;

    @Tool("Create or update a skill following the Agent Skills open standard. " +
            "Creates the skill folder and SKILL.md. " +
            "To add scripts or references, use writeSkillFile. " +
            "Add 'pinned: true' to the frontmatter to protect a skill from background archiving.")
    public String saveSkill(
            @P("Skill name (folder name, e.g. check_disk)") String skillName,
            @P("Full content of SKILL.md including YAML frontmatter:\n---\nname: skill-name\ndescription: when to use this skill\n---\n\n# Instructions\n...") String skillMdContent) {
        if (SkillStore.isUnsafeName(skillName)) return "Error: invalid skill name.";
        try {
            File skillDir = new File(SKILL_DIR, skillName);
            skillDir.mkdirs();
            Files.writeString(new File(skillDir, SkillStore.SKILL_MD).toPath(), skillMdContent);
            return "Skill saved: " + skillName + " (use writeSkillFile to add scripts/ or references/)";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Patch SKILL.md of an existing skill with an exact find-and-replace. " +
            "Use this to fix a wrong or outdated instruction the moment you discover it while following a skill — " +
            "cheaper and safer than rewriting the whole file. oldText must match exactly once; " +
            "if it matches multiple places, retry with a longer anchor.")
    public String patchSkill(
            @P("Skill name (folder name)") String skillName,
            @P("Exact text to find in SKILL.md (must be unique in the file)") String oldText,
            @P("Replacement text") String newText) {
        if (SkillStore.isUnsafeName(skillName)) return "Error: invalid skill name.";
        if (oldText == null || oldText.isEmpty()) return "Error: oldText is empty.";
        File skillMd = new File(SKILL_DIR + skillName, SkillStore.SKILL_MD);
        if (!skillMd.exists()) return "Skill not found: " + skillName;
        try {
            String content = Files.readString(skillMd.toPath());
            int first = content.indexOf(oldText);
            if (first < 0) return "Error: oldText not found in SKILL.md.";
            if (content.indexOf(oldText, first + 1) >= 0) {
                return "Error: oldText matches more than once — use a longer, unique anchor.";
            }
            Files.writeString(skillMd.toPath(), content.substring(0, first) + (null == newText ? "" : newText)
                    + content.substring(first + oldText.length()));
            SkillStore.recordPatch(skillMd.getParentFile().toPath());
            return "Skill patched: " + skillName;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Write a file inside a skill directory, e.g. scripts/run.sh or references/api_docs.md. " +
            "Creates parent directories automatically and overwrites the file if it already exists.")
    public String writeSkillFile(
            @P("Skill name (folder name)") String skillName,
            @P("Relative file path inside the skill folder, e.g. scripts/run.sh") String relativePath,
            @P("Text content to write") String content) {
        if (SkillStore.isUnsafeName(skillName)) return "Error: invalid skill name.";
        if (SkillStore.isUnsafeRelativePath(relativePath)) return "Error: invalid file path.";
        File skillDir = new File(SKILL_DIR, skillName);
        if (!skillDir.isDirectory()) return "Skill not found: " + skillName + " (create it with saveSkill first)";
        try {
            File file = new File(skillDir, relativePath);
            if (null != file.getParentFile()) file.getParentFile().mkdirs();
            Files.writeString(file.toPath(), null == content ? "" : content);
            return "File written: " + skillName + "/" + relativePath;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Delete a skill folder and all its contents.")
    public String deleteSkill(
            @P("Skill name to delete (folder name)") String skillName) {
        if (SkillStore.isUnsafeName(skillName)) return "Error: invalid skill name.";
        File skillDir = new File(SKILL_DIR, skillName);
        if (!skillDir.isDirectory()) return "Skill not found: " + skillName;
        try {
            deleteRecursively(skillDir);
            return "Skill deleted: " + skillName;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory() && file.listFiles() != null) {
            for (File child : file.listFiles()) deleteRecursively(child);
        }
        file.delete();
    }

}
