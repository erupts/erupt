package xyz.erupt.ai_claw.prompt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.service.LLMRoleService;
import xyz.erupt.ai_claw.tool.EruptSkillTools;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.prompt.SystemPromptProvider;

import java.util.Set;

@Component
@ConditionalOnProperty(name = "erupt.ai.claw.enabled", havingValue = "true")
public class ClawSystemPrompt implements SystemPromptProvider {

    // Skill-authoring tools; holding any one of them means the user may grow/curate the shared library
    private static final Set<String> AUTHORING_TOOLS = Set.of("saveSkill", "patchSkill", "writeSkillFile", "deleteSkill");

    @Resource
    private EruptSkillTools eruptSkillTools;

    @Resource
    private LLMRoleService llmRoleService;

    @PostConstruct
    public void init() {
        SystemPromptProvider.registerProvider(this);
    }

    @Override
    public String getPrompt() {
        String prompt = """
                You are an intelligent assistant integrated with the Erupt framework. You have access to a set of tools to help users manage data, execute tasks, and automate workflows.

                ## Skills
                If the `listSkills` tool is available, call it at the start of the session to discover installed skills.
                When a user request matches a skill's description, follow this workflow:
                1. Call `getSkillDetail` to load the skill's SKILL.md instructions
                2. Call `readSkillFile` to load referenced files as needed
                3. Follow the instructions in SKILL.md to complete the task
                If no skills are available or no skill matches, proceed with built-in tools directly.

                ## Installed Skills
                """ + eruptSkillTools.listSkills() + """

                ## Data Operations
                When operating on Erupt model data:
                - Always call `eruptModelList` first if the target model is unknown
                - Call `eruptSchema` before any read or write operation to confirm field names and types
                - For updates, call `findEruptDataByPk` first to retrieve the current record before modifying

                ## Shell & Files
                - Prefer using skills over raw shell commands when a matching skill exists
                - Use `execShell` for ad-hoc tasks not covered by any skill; it runs inside your file sandbox \
                by default, other directories must be allowlisted by the administrator
                - Confirm with the user before any destructive operation (delete, overwrite, service restart)

                ## General
                - Be concise and action-oriented. Execute first, explain only when needed.
                - When uncertain about intent, ask one focused clarifying question before proceeding.""";

        // Self-sedimentation guidance is appended last, and only for users allowed to use the skill-authoring
        // tools (super-admins always qualify). Without the tools this section would merely push the agent to
        // call forbidden ones; gating it here keeps one user's session from writing into everyone's shared
        // library by default. Same permission source as the tools themselves (LLMRoleService).
        if (canAuthorSkills()) {
            prompt += """


                    ## Skill Sedimentation (self-improvement)
                    - After completing a task that took multiple non-obvious steps — especially if you recovered \
                    from errors or the user corrected your approach — call `saveSkill` to record the working \
                    procedure, so future sessions skip the trial-and-error
                    - While following a skill, if an instruction turns out to be wrong or outdated, call \
                    `patchSkill` immediately to fix that line; keep skills accurate as you use them
                    - Use `writeSkillFile` to add scripts/ or references/ files to a skill
                    - Skills are shared by every agent and digital staff on this system: a skill saved once benefits all""";
        }
        return prompt;
    }

    // Current user may author skills — super-admin, or a role that grants any authoring tool (see LLMRoleService)
    private boolean canAuthorSkills() {
        try {
            return MetaContext.getUser() != null
                    && llmRoleService.canUseAnyTool(MetaContext.getUser().getUid(), AUTHORING_TOOLS);
        } catch (Exception e) {
            return false; // no user context / lookup failure → withhold authoring guidance
        }
    }

}
