package xyz.erupt.ai_dev.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.ai_dev.constant.DeliveryStatus;
import xyz.erupt.ai_dev.constant.RequirementStage;
import xyz.erupt.ai_dev.notice.DevNoticeEvent;
import xyz.erupt.ai_dev.notice.DevNotices;
import xyz.erupt.ai_dev.model.DevProject;
import xyz.erupt.ai_dev.model.DevRequirement;
import xyz.erupt.ai_dev.model.DevRequirementDelivery;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.git.GitAuth;
import xyz.erupt.git.GitRepo;
import xyz.erupt.git.model.GitPushResult;
import xyz.erupt.git.model.GitRebaseResult;
import xyz.erupt.jpa.dao.EruptDao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Delivers an accepted requirement: pushes the branch of every repository it changed. A repository with
 * a target branch is rebased onto it first and pushed fast-forward only; without one, the requirement's
 * own branch is pushed. Every repository is checked before the first push, so a conflict in one leaves
 * all of them untouched.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Slf4j
@Service
public class DevDeliveryService {

    private record Plan(DevRunContext.Repo repo, GitRepo gitRepo, GitAuth auth, Path worktree, String target, boolean changed) {
    }

    @Resource
    private DevNotices devNotices;

    @Resource
    private DevRepoService devRepoService;

    @Resource
    private DevWorkspaceService devWorkspaceService;

    @Resource
    private EruptDao eruptDao;

    /**
     * Runs inside the caller's transaction; answers the message shown to the reviewer.
     */
    public String deliver(DevRequirement requirement) {
        DevProject project = requirement.getProject();
        DevRunContext ctx = DevRunContext.of(project, requirement);
        List<Plan> plans = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        for (DevRunContext.Repo repo : ctx.repos()) {
            GitRepo gitRepo = devRepoService.fetch(ctx.tenant(), ctx.projectCode(), repo.alias(), repo.credential());
            GitAuth auth = devRepoService.auth(repo.credential());
            Path worktree = devWorkspaceService.worktree(ctx, repo);
            if (!Files.exists(worktree)) throw new IllegalStateException("Worktree of " + repo.alias() + " is missing, retry the requirement");
            String target = StringUtils.defaultIfBlank(repo.targetBranch(), ctx.workBranch());
            boolean changed = gitRepo.commitsAhead("origin/" + repo.baseBranch(), ctx.workBranch()) > 0;
            if (changed && !target.equals(ctx.workBranch()) && gitRepo.hasRemoteBranch(target)) {
                GitRebaseResult rebased = gitRepo.rebase(worktree, "origin/" + target);
                if (!rebased.ok()) conflicts.add(repo.alias() + ": " + String.join(", ", rebased.conflicts()));
            }
            plans.add(new Plan(repo, gitRepo, auth, worktree, target, changed));
        }
        if (!conflicts.isEmpty()) {
            String message = "Conflicts with the target branch, nothing was pushed. " + String.join("; ", conflicts);
            for (Plan plan : plans) {
                this.record(requirement, plan, conflicts.stream().anyMatch(it -> it.startsWith(plan.repo().alias() + ":"))
                        ? DeliveryStatus.CONFLICT : DeliveryStatus.PENDING, null, message);
            }
            requirement.setStatusMessage(StringUtils.abbreviate(message, AnnotationConst.REMARK_LENGTH));
            return message;
        }
        List<String> problems = new ArrayList<>();
        for (Plan plan : plans) {
            if (!plan.changed()) {
                this.record(requirement, plan, DeliveryStatus.SKIPPED, null, "No changes");
                continue;
            }
            GitPushResult[] result = new GitPushResult[1];
            try {
                devRepoService.locked(ctx.tenant(), ctx.projectCode(), plan.repo().alias(),
                        () -> result[0] = plan.gitRepo().push(plan.worktree(), plan.auth(), plan.target()));
                DeliveryStatus status = switch (result[0].status()) {
                    case PUSHED, UP_TO_DATE -> DeliveryStatus.PUSHED;
                    case REJECTED -> DeliveryStatus.REJECTED;
                    case NON_FAST_FORWARD -> DeliveryStatus.FAILED;
                };
                this.record(requirement, plan, status, plan.gitRepo().head(plan.worktree()), result[0].detail());
                if (!result[0].ok()) problems.add(plan.repo().alias() + ": " + result[0].detail());
            } catch (RuntimeException e) {
                this.record(requirement, plan, DeliveryStatus.FAILED, null, e.getMessage());
                problems.add(plan.repo().alias() + ": " + e.getMessage());
            }
        }
        if (!problems.isEmpty()) {
            String message = "Delivery incomplete. " + String.join("; ", problems);
            requirement.setStatusMessage(StringUtils.abbreviate(message, AnnotationConst.REMARK_LENGTH));
            return message;
        }
        requirement.setStage(RequirementStage.DELIVERED);
        requirement.setStatusMessage(null);
        devNotices.publish(requirement, DevNoticeEvent.Kind.DELIVERED);
        try {
            devWorkspaceService.cleanUp(ctx);
        } catch (IOException | RuntimeException e) {
            log.warn("Workspace of {} was delivered but not cleaned up: {}", ctx.code(), e.getMessage());
        }
        return null;
    }

    private void record(DevRequirement requirement, Plan plan, DeliveryStatus status, String commit, String message) {
        DevRequirementDelivery delivery = new DevRequirementDelivery();
        delivery.setProject(requirement.getProject());
        delivery.setRequirement(requirement);
        delivery.setRepoAlias(plan.repo().alias());
        delivery.setBranch(plan.target());
        delivery.setCommitSha(commit);
        delivery.setStatus(status);
        delivery.setMessage(StringUtils.abbreviate(message, AnnotationConst.REMARK_LENGTH));
        delivery.setDeliveredAt(LocalDateTime.now());
        eruptDao.persist(delivery);
    }

}
