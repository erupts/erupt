package xyz.erupt.ai_claw.skill;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the skill storage layer's pure parsing and usage-tracking
 * logic. Every method under test takes an explicit skill directory, so these
 * run against a {@link TempDir} with no home directory or Spring context.
 *
 * @author YuePeng
 */
public class SkillStoreTest {

    private Path writeSkill(Path dir, String skillMd) throws IOException {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(SkillStore.SKILL_MD), skillMd);
        return dir;
    }

    @Test
    public void readsDescriptionFromFrontmatter(@TempDir Path tmp) throws IOException {
        Path skill = writeSkill(tmp.resolve("demo"),
                "---\nname: demo\ndescription: Turn CSV into a chart\n---\n# Body\ndescription: not this one\n");
        // Only the frontmatter description counts; a later body line must be ignored
        assertEquals("Turn CSV into a chart", SkillStore.readDescription(skill));
    }

    @Test
    public void descriptionNullWhenAbsentOrOutsideFrontmatter(@TempDir Path tmp) throws IOException {
        Path noFrontmatter = writeSkill(tmp.resolve("a"), "# No frontmatter\ndescription: ignored\n");
        assertNull(SkillStore.readDescription(noFrontmatter));
        Path noDescription = writeSkill(tmp.resolve("b"), "---\nname: b\n---\nbody\n");
        assertNull(SkillStore.readDescription(noDescription));
    }

    @Test
    public void detectsPinnedFlagToleratingWhitespace(@TempDir Path tmp) throws IOException {
        assertTrue(SkillStore.isPinned(writeSkill(tmp.resolve("p1"), "---\nname: p1\npinned: true\n---\n")));
        assertTrue(SkillStore.isPinned(writeSkill(tmp.resolve("p2"), "---\nname: p2\npinned:true\n---\n")));
        assertFalse(SkillStore.isPinned(writeSkill(tmp.resolve("p3"), "---\nname: p3\npinned: false\n---\n")));
        assertFalse(SkillStore.isPinned(writeSkill(tmp.resolve("p4"), "---\nname: p4\n---\n")));
    }

    @Test
    public void usageDefaultsToZeroWhenNoFile(@TempDir Path tmp) throws IOException {
        Path skill = writeSkill(tmp.resolve("u"), "---\nname: u\n---\n");
        SkillStore.Usage usage = SkillStore.readUsage(skill);
        assertEquals(0, usage.getUses());
        assertEquals(0, usage.getPatches());
        assertNull(usage.getLastUsed());
    }

    @Test
    public void recordUseAndPatchAccumulateAndPersist(@TempDir Path tmp) throws IOException {
        Path skill = writeSkill(tmp.resolve("acc"), "---\nname: acc\n---\n");
        SkillStore.recordUse(skill);
        SkillStore.recordUse(skill);
        SkillStore.recordPatch(skill);
        // Re-read from disk to prove persistence rather than in-memory state
        SkillStore.Usage usage = SkillStore.readUsage(skill);
        assertEquals(2, usage.getUses());
        assertEquals(1, usage.getPatches());
        assertNotNull(usage.getLastUsed());
        assertTrue(Files.exists(skill.resolve(SkillStore.USAGE_FILE)));
    }

}
