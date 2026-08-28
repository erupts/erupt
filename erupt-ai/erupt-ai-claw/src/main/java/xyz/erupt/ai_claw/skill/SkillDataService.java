package xyz.erupt.ai_claw.skill;

import org.springframework.stereotype.Service;
import xyz.erupt.ai_claw.skill.SkillStore.Usage;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.view.EruptModel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * In-memory data source for the Skill library page: rows are enumerated from the
 * skill directories under SkillStore.root() on every query, so newly created or
 * archived skills show up without a restart. Read-only; the full SKILL.md body is
 * loaded lazily in findDataById for the detail view.
 *
 * @author YuePeng
 * date 2026/8/28
 */
@Service
public class SkillDataService extends EruptBeanDataService<EruptSkill> {

    public static final String DATA_PROCESSOR = "ERUPT_AI_SKILL";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, SkillDataService.class);
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        EruptSkill skill = (EruptSkill) super.findDataById(eruptModel, id);
        if (null != skill) {
            Path skillMd = SkillStore.root().resolve(skill.getName()).resolve(SkillStore.SKILL_MD);
            try {
                if (Files.exists(skillMd)) skill.setContent(Files.readString(skillMd));
            } catch (Exception ignored) {
            }
        }
        return skill;
    }

    @Override
    protected List<EruptSkill> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        List<EruptSkill> list = new ArrayList<>();
        try (Stream<Path> dirs = Files.list(SkillStore.root())) {
            dirs.filter(Files::isDirectory)
                    .filter(dir -> !dir.getFileName().toString().startsWith("."))
                    .filter(dir -> Files.exists(dir.resolve(SkillStore.SKILL_MD)))
                    .sorted()
                    .forEach(dir -> list.add(toSkill(dir)));
        } catch (Exception ignored) {
        }
        return list;
    }

    private EruptSkill toSkill(Path dir) {
        EruptSkill skill = new EruptSkill();
        skill.setName(dir.getFileName().toString());
        skill.setDescription(SkillStore.readDescription(dir));
        skill.setPinned(SkillStore.isPinned(dir));
        Usage usage = SkillStore.readUsage(dir);
        skill.setUses(usage.getUses());
        skill.setPatches(usage.getPatches());
        skill.setLastUsed(usage.getLastUsed());
        return skill;
    }

}
