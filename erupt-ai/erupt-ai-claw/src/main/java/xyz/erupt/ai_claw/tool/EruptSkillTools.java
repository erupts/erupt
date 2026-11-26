package xyz.erupt.ai_claw.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_claw.skill.SkillStore;
import xyz.erupt.annotation.ai.AiToolbox;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


/**
 * Tools that read the shared skill library; writing it is {@link EruptSkillAuthorTools}.
 */
@AiToolbox(platform = true)
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class EruptSkillTools {

    private static final String SKILL_DIR = SkillStore.root() + File.separator;

    // ======================== Skills ========================

    @Tool("List all available skills. Returns skill names and descriptions parsed from SKILL.md frontmatter.")
    public String listSkills() {
        File dir = new File(SKILL_DIR);
        if (!dir.exists() || dir.listFiles() == null) {
            return "Skill directory is empty or not found: " + SKILL_DIR;
        }
        List<String> result = new ArrayList<>();
        for (File entry : dir.listFiles()) {
            if (!entry.isDirectory()) continue;
            File skillMd = new File(entry, SkillStore.SKILL_MD);
            if (!skillMd.exists()) continue;
            String description = SkillStore.readDescription(entry.toPath());
            result.add(entry.getName() + ": " + (null == description ? "(no description)" : description));
        }
        if (result.isEmpty()) return "No skills found in: " + SKILL_DIR;
        return String.join("\n", result);
    }

    @Tool("Get detail of a skill, including SKILL.md content and available files in scripts/ and references/. " +
            "Call this before executing any skill script to understand its structure and usage.")
    public String getSkillDetail(
            @P("Skill name (folder name, e.g. check_disk)") String skillName) {
        if (SkillStore.isUnsafeName(skillName)) return "Error: invalid skill name.";
        File skillDir = new File(SKILL_DIR, skillName);
        if (!skillDir.isDirectory()) return "Skill not found: " + skillName;

        File skillMd = new File(skillDir, SkillStore.SKILL_MD);
        if (!skillMd.exists()) return "Error: SKILL.md not found in skill: " + skillName;

        StringBuilder sb = new StringBuilder();
        sb.append("### SKILL.md\n").append(readFileContent(skillMd));

        File scriptsDir = new File(skillDir, "scripts");
        if (scriptsDir.exists() && scriptsDir.listFiles() != null) {
            sb.append("\n\n### scripts/\n");
            for (File f : scriptsDir.listFiles()) {
                sb.append("- ").append(f.getName()).append("\n");
            }
        }

        File refsDir = new File(skillDir, "references");
        if (refsDir.exists() && refsDir.listFiles() != null) {
            sb.append("\n### references/\n");
            for (File f : refsDir.listFiles()) {
                sb.append("- ").append(f.getName()).append("\n");
            }
        }

        // Loading a skill counts as using it; the curator relies on this signal
        SkillStore.recordUse(skillDir.toPath());
        return sb.toString();
    }

    @Tool("Read a specific file inside a skill directory for progressive disclosure. " +
            "Use to load only what is needed, e.g. references/api_docs.md or scripts/run.sh.")
    public String readSkillFile(
            @P("Skill name (folder name)") String skillName,
            @P("Relative file path inside the skill folder, e.g. references/api_docs.md or scripts/run.sh") String relativePath) {
        if (SkillStore.isUnsafeName(skillName) || relativePath == null || relativePath.contains("..")) {
            return "Error: invalid path.";
        }
        File file = new File(SKILL_DIR + skillName + "/" + relativePath);
        if (!file.exists()) return "File not found: " + relativePath;
        return readFileContent(file);
    }

    // ======================== Private helpers ========================

    private String readFileContent(File f) {
        try {
            return Files.readString(f.toPath());
        } catch (Exception e) {
            return "(unreadable: " + e.getMessage() + ")";
        }
    }
}
