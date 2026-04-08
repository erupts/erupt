package xyz.erupt.ai_claw.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.annotation.AiToolbox;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


@AiToolbox
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class EruptSkillTools {

    private static final String SKILL_DIR =
            System.getProperty("user.home") + "/.erupt-ai/skills/";

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
            File skillMd = new File(entry, "SKILL.md");
            if (!skillMd.exists()) continue;
            result.add(entry.getName() + ": " + readDescription(skillMd));
        }
        if (result.isEmpty()) return "No skills found in: " + SKILL_DIR;
        return String.join("\n", result);
    }

    @Tool("Get detail of a skill, including SKILL.md content and available files in scripts/ and references/. " +
            "Call this before executing any skill script to understand its structure and usage.")
    public String getSkillDetail(
            @P("Skill name (folder name, e.g. check_disk)") String skillName) {
        if (isUnsafePath(skillName)) return "Error: invalid skill name.";
        File skillDir = new File(SKILL_DIR, skillName);
        if (!skillDir.isDirectory()) return "Skill not found: " + skillName;

        File skillMd = new File(skillDir, "SKILL.md");
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

        return sb.toString();
    }

    @Tool("Read a specific file inside a skill directory for progressive disclosure. " +
            "Use to load only what is needed, e.g. references/api_docs.md or scripts/run.sh.")
    public String readSkillFile(
            @P("Skill name (folder name)") String skillName,
            @P("Relative file path inside the skill folder, e.g. references/api_docs.md or scripts/run.sh") String relativePath) {
        if (isUnsafePath(skillName) || relativePath == null || relativePath.contains("..")) {
            return "Error: invalid path.";
        }
        File file = new File(SKILL_DIR + skillName + "/" + relativePath);
        if (!file.exists()) return "File not found: " + relativePath;
        return readFileContent(file);
    }

    @Tool("Create or update a skill following the Agent Skills open standard. " +
            "Creates the skill folder and SKILL.md. " +
            "To add scripts or references, use execShell to write files into the skill directory.")
    public String saveSkill(
            @P("Skill name (folder name, e.g. check_disk)") String skillName,
            @P("Full content of SKILL.md including YAML frontmatter:\n---\nname: skill-name\ndescription: when to use this skill\n---\n\n# Instructions\n...") String skillMdContent) {
        if (isUnsafePath(skillName)) return "Error: invalid skill name.";
        try {
            File skillDir = new File(SKILL_DIR, skillName);
            skillDir.mkdirs();
            Files.writeString(new File(skillDir, "SKILL.md").toPath(), skillMdContent);
            return "Skill saved: " + skillName + " (use execShell to add scripts/ or references/)";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool("Delete a skill folder and all its contents.")
    public String deleteSkill(
            @P("Skill name to delete (folder name)") String skillName) {
        if (isUnsafePath(skillName)) return "Error: invalid skill name.";
        File skillDir = new File(SKILL_DIR, skillName);
        if (!skillDir.isDirectory()) return "Skill not found: " + skillName;
        try {
            deleteRecursively(skillDir);
            return "Skill deleted: " + skillName;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // ======================== Private helpers ========================

    private String readDescription(File skillMd) {
        try {
            boolean inFrontmatter = false;
            for (String line : Files.readAllLines(skillMd.toPath())) {
                if (line.trim().equals("---")) {
                    inFrontmatter = !inFrontmatter;
                    continue;
                }
                if (inFrontmatter && line.startsWith("description:")) {
                    return line.replace("description:", "").trim();
                }
            }
        } catch (Exception ignored) {
        }
        return "(no description)";
    }

    private String readFileContent(File f) {
        try {
            return Files.readString(f.toPath());
        } catch (Exception e) {
            return "(unreadable: " + e.getMessage() + ")";
        }
    }

    private boolean isUnsafePath(String name) {
        return name == null || name.contains("..") || name.contains("\\") || name.startsWith("/");
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory() && file.listFiles() != null) {
            for (File child : file.listFiles()) deleteRecursively(child);
        }
        file.delete();
    }
}