package xyz.erupt.ai_dev.service;

import org.apache.commons.lang3.StringUtils;
import xyz.erupt.ai_dev.runner.ClaudeCodeRunner;

import xyz.erupt.ai_dev.constant.AgentRole;
import xyz.erupt.ai_dev.model.DevProject;
import xyz.erupt.ai_dev.model.DevRequirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * What a run needs of its requirement and project, copied out of the database once, so the hour-long
 * work around it touches no entity.
 *
 * @author YuePeng
 * date 2026/10/22
 */
public record DevRunContext(String tenant, Long projectId, String projectCode, String projectName, String agentsMd,
                            List<Repo> repos, List<Agent> agents,
                            Long requirementId, String code, String title, String description, String instruction) {

    public record Repo(String alias, String baseBranch, String targetBranch, String credential) {
    }

    public record Agent(String name, AgentRole role, String executor, String model) {
    }

    public static DevRunContext of(DevProject project, DevRequirement requirement) {
        List<Repo> repos = project.getRepos().stream()
                .filter(it -> requirement.getRepos().contains(it.getAlias()))
                .sorted((a, b) -> a.getAlias().compareTo(b.getAlias()))
                .map(it -> new Repo(it.getAlias(), it.getBaseBranch(), it.getTargetBranch(), it.getCredential())).toList();
        // the requirement names its executor; the QA run, when asked for, uses the same one
        String executor = StringUtils.defaultIfBlank(requirement.getExecutor(), ClaudeCodeRunner.CODE);
        String model = StringUtils.trimToNull(requirement.getModel());
        List<Agent> agents = new ArrayList<>();
        agents.add(new Agent(executor, AgentRole.DEVELOPER, executor, model));
        if (Boolean.TRUE.equals(requirement.getQaReview())) agents.add(new Agent(executor + "-qa", AgentRole.QA, executor, model));
        return new DevRunContext(project.getTenantId(), project.getId(), project.getCode(), project.getName(), project.getAgentsMd(),
                repos, agents, requirement.getId(), requirement.getCode(), requirement.getTitle(), requirement.getDescription(),
                requirement.getInstruction());
    }

    public Optional<Agent> agent(AgentRole role) {
        return agents.stream().filter(it -> it.role() == role).findFirst();
    }

    // req/req-12: the branch every worktree of the requirement checks out
    public String workBranch() {
        return "req/" + DevLayout.requirementDir(code);
    }

}
