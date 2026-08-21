package xyz.erupt.ai_claw.skill;

import java.util.Set;

/**
 * Extension point for other modules to protect skills from curator archiving,
 * e.g. a staff module can retain skills referenced by scheduled task instructions.
 * All beans implementing this interface are consulted on every curation run.
 *
 * @author YuePeng
 * date 2026/8/20
 */
public interface SkillRetentionProvider {

    Set<String> retainedSkillNames();

}
