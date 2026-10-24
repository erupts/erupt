package xyz.erupt.ai_dev.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.ai_dev.constant.DeliveryStatus;
import xyz.erupt.ai_dev.constant.*;
import xyz.erupt.ai_dev.runner.ClaudeCodeRunner;
import xyz.erupt.ai_dev.runner.CodingAgentRunner;
import xyz.erupt.ai_dev.runner.DevRunners;
import xyz.erupt.ai_dev.model.*;
import xyz.erupt.ai_dev.service.DevAccessService;
import xyz.erupt.ai_dev.service.DevExecutor;
import xyz.erupt.ai_dev.service.DevRepoService;
import xyz.erupt.ai_dev.service.DevRunContext;
import xyz.erupt.ai_dev.service.DevWorkspaceService;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.view.R;
import xyz.erupt.git.GitException;
import xyz.erupt.git.GitRepo;
import xyz.erupt.jpa.dao.EruptDao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

/**
 * AI dev requirement pool
 *
 * @order 59
 * @apiNote Read side of the requirement pool page: projects, requirements, a requirement with its agent
 * sessions and deliveries, and the diff of its branches. Writes go through the DevRequirement model's own
 * add API and row operations. Every call needs the pool page's menu and membership of the project.
 *
 * @author YuePeng
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/ai-dev/pool")
public class DevPoolController implements EruptRouter.VerifyHandler {

    public static final String PAGE = "/ai-dev/pool";

    // a review diff larger than this is cut, the files list still names every file
    private static final int DIFF_LIMIT = 400_000;

    public record Project(Long id, String code, String name, MemberRole role, List<String> repos) {
    }

    /**
     * @param mine     created by the current user
     * @param toReview waiting for the current user: in review or acceptance, and theirs to review
     */
    public record Row(Long id, String code, String title, RequirementStage stage, Priority priority, Integer progress,
                      String phase, Set<String> repos, String verdict, String statusMessage, String createBy, String reviewer,
                      String agent, LocalDateTime createTime, LocalDateTime updateTime, boolean mine, boolean toReview,
                      String createByAvatar, String reviewerAvatar, String model, String executor, String executorIcon,
                      Map<String, RepoState> deliveries) {

        Row withDeliveries(Map<String, RepoState> deliveries) {
            return new Row(id, code, title, stage, priority, progress, phase, repos, verdict, statusMessage, createBy, reviewer,
                    agent, createTime, updateTime, mine, toReview, createByAvatar, reviewerAvatar, model, executor, executorIcon, deliveries);
        }
    }

    public record Session(Long id, AgentRole role, String agentName, String executor, SessionStatus status,
                          String summary, Double costUsd, Long tokens, LocalDateTime startedAt, LocalDateTime endedAt) {
    }

    public record Delivery(String repoAlias, String branch, String commitSha, DeliveryStatus status, String message,
                           LocalDateTime deliveredAt) {
    }

    public record Detail(Row requirement, String description, Integer version, Long projectId, String projectName, String qaAgent,
                         List<Session> sessions, List<Delivery> deliveries, boolean canReview, boolean executorEnabled,
                         boolean canApprove) {
    }

    public record RepoDiff(String alias, String base, String branch, List<String> files, String patch, boolean truncated) {
    }

    @Resource
    private EruptDao eruptDao;

    @Resource
    private DevAccessService devAccessService;

    @Resource
    private DevRepoService devRepoService;

    @Resource
    private DevExecutor devExecutor;

    @Resource
    private DevRunners devRunners;

    @Resource
    private DevWorkspaceService devWorkspaceService;

    @Override
    public String convertAuthStr(EruptRouter eruptRouter, HttpServletRequest request, String authStr) {
        return PAGE;
    }

    /**
     * Projects the current user is a member of.
     */
    @GetMapping("/projects")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = DevPoolController.class)
    public R<List<Project>> projects() {
        return R.ok(devAccessService.visibleProjects().stream().map(p -> new Project(p.getId(), p.getCode(), p.getName(),
                devAccessService.role(p.getId()), p.getRepos().stream().map(DevProjectRepo::getAlias).sorted().toList())).toList());
    }

    /**
     * The coding CLIs installed on this server, for the new-requirement form, the one it starts with, and
     * whether this instance runs agents at all.
     *
     * @param project project id
     */
    @GetMapping("/executors")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = DevPoolController.class)
    public R<Executors> executors(@RequestParam("project") Long project) {
        devAccessService.requireMember(project);
        return R.ok(new Executors(devRunners.installed().stream().map(it -> new Executor(it.code(), it.label(), devRunners.models(it), it.icon())).toList(),
                devRunners.preferred(project, devAccessService.user()), devExecutor.enabled()));
    }

    /**
     * @param models model ids to suggest, empty when the CLI cannot tell
     */
    /**
     * The latest delivery of one repository: where it went and how.
     */
    public record RepoState(DeliveryStatus status, String branch, String message) {
    }

    public record Executor(String code, String label, List<String> models, String icon) {
    }

    /**
     * @param enabled whether this instance runs queued requirements; when no instance does, they stay queued
     */
    public record Executors(List<Executor> options, String preferred, boolean enabled) {
    }

    /**
     * Requirements of a project, most recently changed first.
     *
     * @param project project id
     */
    @GetMapping("/requirements")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = DevPoolController.class)
    public R<List<Row>> requirements(@RequestParam("project") Long project) {
        devAccessService.requireMember(project);
        DevProject p = eruptDao.find(DevProject.class, project);
        boolean review = this.canReview(project);
        // each requirement's latest delivery per repository, in one query for the whole list
        Map<Long, Map<String, RepoState>> deliveries = new HashMap<>();
        eruptDao.getEntityManager().createQuery(
                        "select d from DevRequirementDelivery d where d.project.id = :p order by d.id", DevRequirementDelivery.class)
                .setParameter("p", project).getResultList()
                .forEach(d -> deliveries.computeIfAbsent(d.getRequirement().getId(), k -> new LinkedHashMap<>())
                        .put(d.getRepoAlias(), new RepoState(d.getStatus(), d.getBranch(), d.getMessage())));
        return R.ok(eruptDao.getEntityManager().createQuery(
                        "select r from DevRequirement r where r.project.id = :p order by r.updateTime desc, r.id desc", DevRequirement.class)
                .setParameter("p", project).getResultList().stream()
                .map(r -> this.row(r, p, review).withDeliveries(deliveries.getOrDefault(r.getId(), Map.of()))).toList());
    }

    /**
     * A requirement with its agent sessions and deliveries.
     *
     * @param id requirement id
     */
    @GetMapping("/requirement/{id}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = DevPoolController.class)
    public R<Detail> requirement(@PathVariable("id") Long id) {
        DevRequirement r = this.load(id);
        List<Session> sessions = eruptDao.getEntityManager().createQuery(
                        "select s from DevAgentSession s where s.requirement.id = :id order by s.id desc", DevAgentSession.class)
                .setParameter("id", id).getResultList().stream()
                .map(s -> new Session(s.getId(), s.getRole(), s.getAgentName(), s.getExecutor(), s.getStatus(), s.getSummary(),
                        s.getCostUsd(), s.getTokens(), s.getStartedAt(), s.getEndedAt())).toList();
        List<Delivery> deliveries = eruptDao.getEntityManager().createQuery(
                        "select d from DevRequirementDelivery d where d.requirement.id = :id order by d.id desc", DevRequirementDelivery.class)
                .setParameter("id", id).getResultList().stream()
                .map(d -> new Delivery(d.getRepoAlias(), d.getBranch(), d.getCommitSha(), d.getStatus(), d.getMessage(), d.getDeliveredAt())).toList();
        boolean review = this.canReview(r.getProject().getId());
        return R.ok(new Detail(this.row(r, r.getProject(), review), r.getDescription(), r.getVersion(), r.getProject().getId(), r.getProject().getName(),
                Boolean.TRUE.equals(r.getQaReview()) ? this.agent(r) + " QA" : null, sessions, deliveries, review, devExecutor.enabled(),
                devAccessService.mayApprove(r)));
    }

    /**
     * The requirement's workspace on the server, one entry per repository: whether its worktree is there,
     * how far its branch is ahead of the base and what is left uncommitted. Paths are shown to super admins only.
     *
     * @param id requirement id
     */
    @GetMapping("/requirement/{id}/workspace")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = DevPoolController.class)
    public R<List<DevWorkspaceService.Workspace>> workspace(@PathVariable("id") Long id) {
        DevRequirement r = this.load(id);
        boolean admin = devAccessService.superAdmin();
        return R.ok(devWorkspaceService.describe(DevRunContext.of(r.getProject(), r)).stream()
                .map(w -> admin ? w : new DevWorkspaceService.Workspace(w.alias(), w.branch(), w.base(), w.exists(), w.ahead(), w.changed(), null))
                .toList());
    }

    /**
     * What the requirement's branch changes in each repository, against the latest base branch.
     *
     * @param id requirement id
     */
    @GetMapping("/requirement/{id}/diff")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, verifyHandler = DevPoolController.class)
    public R<List<RepoDiff>> diff(@PathVariable("id") Long id) {
        DevRequirement r = this.load(id);
        DevRunContext ctx = DevRunContext.of(r.getProject(), r);
        List<RepoDiff> diffs = new ArrayList<>();
        for (DevRunContext.Repo repo : ctx.repos()) {
            String base = "origin/" + repo.baseBranch();
            try {
                GitRepo gitRepo = devRepoService.open(ctx.tenant(), ctx.projectCode(), repo.alias());
                if (!gitRepo.hasBranch(ctx.workBranch())) {
                    diffs.add(new RepoDiff(repo.alias(), base, ctx.workBranch(), List.of(), "", false));
                    continue;
                }
                String patch = gitRepo.diff(base, ctx.workBranch());
                diffs.add(new RepoDiff(repo.alias(), base, ctx.workBranch(), gitRepo.changedFiles(base, ctx.workBranch()),
                        StringUtils.left(patch, DIFF_LIMIT), patch.length() > DIFF_LIMIT));
            } catch (GitException e) {
                throw new EruptWebApiRuntimeException(repo.alias() + ": " + e.getMessage());
            }
        }
        return R.ok(diffs);
    }

    private DevRequirement load(Long id) {
        DevRequirement r = eruptDao.find(DevRequirement.class, id);
        if (null == r) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.project_not_found"));
        devAccessService.requireMember(r.getProject().getId());
        return r;
    }

    private Row row(DevRequirement r, DevProject project, boolean canReview) {
        MetaUserinfo me = devAccessService.user();
        boolean mine = devAccessService.isCreator(r);
        boolean waiting = r.getStage() == RequirementStage.REVIEW || r.getStage() == RequirementStage.ACCEPTANCE;
        // one's own requirement awaits someone else's approval unless the project allows self review
        boolean approvable = r.getStage() != RequirementStage.REVIEW || !mine || Boolean.TRUE.equals(project.getSelfReview());
        boolean toReview = waiting && canReview && approvable
                && (null == r.getReviewer() || (null != me && r.getReviewer().getId().equals(me.getId())));
        return new Row(r.getId(), r.getCode(), r.getTitle(), r.getStage(), r.getPriority(), r.getProgress(), r.getPhase(),
                r.getRepos(), r.getVerdict(), r.getStatusMessage(), r.getCreateBy(),
                null == r.getReviewer() ? null : r.getReviewer().getName(), this.agent(r),
                r.getCreateTime(), r.getUpdateTime(), mine, toReview,
                null == r.getCreator() ? null : r.getCreator().getAvatar(), null == r.getReviewer() ? null : r.getReviewer().getAvatar(),
                StringUtils.trimToNull(r.getModel()), this.executorCode(r),
                devRunners.find(this.executorCode(r)).map(CodingAgentRunner::icon).orElse(null), Map.of());
    }

    private boolean canReview(Long projectId) {
        MemberRole role = devAccessService.role(projectId);
        return null != role && (role.manages() || role == MemberRole.REVIEWER);
    }

    // the executor's name as people know it, e.g. Claude Code
    private String agent(DevRequirement r) {
        String code = this.executorCode(r);
        return devRunners.find(code).map(CodingAgentRunner::label).orElse(code);
    }

    // its code, as the runner knows it: an older claude_code reads as claude
    private String executorCode(DevRequirement r) {
        String code = StringUtils.defaultIfBlank(r.getExecutor(), ClaudeCodeRunner.CODE);
        return devRunners.find(code).map(CodingAgentRunner::code).orElse(code);
    }

}
