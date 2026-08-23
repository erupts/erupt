package xyz.erupt.ai_claw.skill;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_claw.prop.EruptAiClawProp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Background maintenance of the skill library: skills unused for longer than
 * the configured stale window are moved to .archive (never deleted), keeping
 * the skill list — which is injected into every system prompt — lean.
 * Exempt: pinned skills (frontmatter "pinned: true") and skills reported by
 * any SkillRetentionProvider bean. Every action is appended to the ledger.
 *
 * @author YuePeng
 * date 2026/8/20
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class SkillCurator {

    @Resource
    private EruptAiClawProp eruptAiClawProp;

    private final ObjectProvider<SkillRetentionProvider> retentionProviders;

    public SkillCurator(ObjectProvider<SkillRetentionProvider> retentionProviders) {
        this.retentionProviders = retentionProviders;
    }

    @Scheduled(cron = "0 0 4 * * ?")
    public void curate() {
        if (!eruptAiClawProp.isSkillCuratorEnabled()) return;
        Set<String> retained = new HashSet<>();
        retentionProviders.forEach(provider -> retained.addAll(provider.retainedSkillNames()));
        Path root = SkillStore.root();
        Instant staleBefore = Instant.now().minus(eruptAiClawProp.getSkillStaleDays(), ChronoUnit.DAYS);
        try (Stream<Path> dirs = Files.list(root)) {
            dirs.filter(Files::isDirectory)
                    .filter(dir -> !dir.getFileName().toString().startsWith("."))
                    .filter(dir -> Files.exists(dir.resolve(SkillStore.SKILL_MD)))
                    .filter(dir -> !retained.contains(dir.getFileName().toString()))
                    .filter(dir -> !SkillStore.isPinned(dir))
                    .filter(dir -> lastUsed(dir).isBefore(staleBefore))
                    .forEach(this::archive);
        } catch (Exception e) {
            log.error("Skill curation failed", e);
        }
    }

    // Last-used instant from .usage.json, falling back to SKILL.md modification time
    private Instant lastUsed(Path skillDir) {
        String lastUsed = SkillStore.readUsage(skillDir).getLastUsed();
        if (null != lastUsed) {
            try {
                return java.time.LocalDateTime.parse(lastUsed, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .atZone(java.time.ZoneId.systemDefault()).toInstant();
            } catch (Exception ignored) {
            }
        }
        try {
            return Files.getLastModifiedTime(skillDir.resolve(SkillStore.SKILL_MD)).toInstant();
        } catch (Exception e) {
            return Instant.now(); // unreadable → treat as fresh, never archive blindly
        }
    }

    private void archive(Path skillDir) {
        String name = skillDir.getFileName().toString();
        try {
            Path archiveDir = SkillStore.root().resolve(SkillStore.ARCHIVE_DIR);
            Files.createDirectories(archiveDir);
            Path target = archiveDir.resolve(name);
            if (Files.exists(target)) {
                target = archiveDir.resolve(name + "-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
            }
            Files.move(skillDir, target);
            SkillStore.ledger("archive", name, "unused for over " + eruptAiClawProp.getSkillStaleDays() + " days");
            log.info("Skill archived by curator: {} -> {}", name, target);
        } catch (Exception e) {
            log.error("Cannot archive skill: {}", name, e);
        }
    }

}
