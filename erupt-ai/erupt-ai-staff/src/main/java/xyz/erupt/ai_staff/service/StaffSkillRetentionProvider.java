package xyz.erupt.ai_staff.service;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_claw.skill.SkillRetentionProvider;
import xyz.erupt.ai_claw.skill.SkillStore;
import xyz.erupt.ai_staff.model.AiStaffTask;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Protects skills referenced by any enabled staff task from being archived by
 * the SkillCurator. A scheduled task's instruction is free-form markdown that
 * may lean on a skill without ever calling getSkillDetail, so usage tracking
 * alone would let the curator archive a skill a live work order depends on.
 * Matching is intentionally permissive (substring, case-insensitive): over-
 * retaining a skill is harmless, silently archiving a needed one is not.
 *
 * @author YuePeng
 * date 2026/8/20
 */
@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class StaffSkillRetentionProvider implements SkillRetentionProvider {

    @Resource
    private EruptDao eruptDao;

    @Override
    public Set<String> retainedSkillNames() {
        Set<String> skillNames = SkillStore.skillNames();
        if (skillNames.isEmpty()) return Set.of();
        List<AiStaffTask> tasks = eruptDao.lambdaQuery(AiStaffTask.class)
                .eq(AiStaffTask::getEnable, true)
                .list();
        Set<String> retained = new HashSet<>();
        for (AiStaffTask task : tasks) {
            if (StringUtils.isBlank(task.getInstruction())) continue;
            String haystack = task.getInstruction().toLowerCase(Locale.ROOT);
            for (String name : skillNames) {
                if (haystack.contains(name.toLowerCase(Locale.ROOT))) retained.add(name);
            }
        }
        return retained;
    }

}
