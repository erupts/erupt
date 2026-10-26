package xyz.erupt.ai_dev.proxy;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.constant.RequirementStage;
import xyz.erupt.ai_dev.model.DevProject;
import xyz.erupt.ai_dev.model.DevProjectRepo;
import xyz.erupt.ai_dev.model.DevRequirement;
import xyz.erupt.ai_dev.runner.DevRunners;
import xyz.erupt.ai_dev.service.DevAccessService;
import xyz.erupt.ai_dev.service.DevRunContext;
import xyz.erupt.ai_dev.service.DevReviewService;
import xyz.erupt.ai_dev.service.DevWorkspaceService;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUserVo;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Numbers a requirement within its project (REQ-1, REQ-2 ...) and keeps its repositories inside that
 * project. The project itself is bound by the page-header scope.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Component
public class DevRequirementProxy implements DataProxy<DevRequirement> {

    @Resource
    private DevWorkspaceService devWorkspaceService;

    @Resource
    private DevRunners devRunners;

    @Resource
    private DevAccessService devAccessService;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private DevReviewService devReviewService;

    @Override
    public void beforeAdd(DevRequirement requirement) {
        DevProject project = this.project(requirement);
        Integer max = eruptDao.getEntityManager()
                .createQuery("select max(r.seq) from DevRequirement r where r.project.id = :pid", Integer.class)
                .setParameter("pid", project.getId()).getSingleResult();
        // a concurrent add taking the same number is stopped by the (project, seq) unique key
        requirement.setSeq(null == max ? 1 : max + 1);
        requirement.setCode("REQ-" + requirement.getSeq());
        requirement.setStage(RequirementStage.DRAFT);
        requirement.setProgress(0);
        MetaUserinfo user = devAccessService.user();
        if (null != user) requirement.setCreator(eruptDao.find(EruptUserVo.class, user.getId()));
        if (StringUtils.isBlank(requirement.getExecutor())) requirement.setExecutor(devRunners.preferred(project.getId(), user));
        this.checkExecutor(requirement);
        this.checkRepos(requirement, project);
    }

    // only a CLI this server can run
    private void checkExecutor(DevRequirement requirement) {
        if (StringUtils.isBlank(requirement.getExecutor())) return;
        if (devRunners.find(requirement.getExecutor()).filter(devRunners::installed).isEmpty()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.executor_unavailable").replace("{0}", requirement.getExecutor()));
        }
    }

    // the definition is frozen while an agent works from it, and once it is delivered
    private static final Set<RequirementStage> FROZEN = EnumSet.of(RequirementStage.QUEUED, RequirementStage.RUNNING,
            RequirementStage.VERIFYING, RequirementStage.DELIVERED);

    @Override
    public void beforeUpdate(DevRequirement requirement) {
        if (FROZEN.contains(requirement.getStage())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.requirement_frozen"));
        }
        this.checkExecutor(requirement);
        this.checkRepos(requirement, this.project(requirement));
    }

    @Override
    public void beforeDelete(DevRequirement requirement) {
        if (FROZEN.contains(requirement.getStage())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.requirement_frozen"));
        }
        // its creator, or whoever manages the project
        Long project = this.project(requirement).getId();
        if (!devAccessService.isCreator(requirement) && !devAccessService.manages(project)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.no_delete_permission"));
        }
        // nothing of it stays behind: its worktrees and branch, then its runs, their logs and deliveries
        try {
            devWorkspaceService.discard(DevRunContext.of(this.project(requirement), requirement));
        } catch (IOException | RuntimeException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.cleanup_failed") + " " + e.getMessage());
        }
        var em = eruptDao.getEntityManager();
        em.createQuery("delete from DevAgentEvent e where e.session.id in "
                + "(select s.id from DevAgentSession s where s.requirement.id = :id)").setParameter("id", requirement.getId()).executeUpdate();
        em.createQuery("delete from DevAgentSession s where s.requirement.id = :id").setParameter("id", requirement.getId()).executeUpdate();
        em.createQuery("delete from DevRequirementDelivery d where d.requirement.id = :id").setParameter("id", requirement.getId()).executeUpdate();
        devReviewService.delete(requirement.getId());
    }

    private DevProject project(DevRequirement requirement) {
        DevProject project = null == requirement.getProject() ? null : eruptDao.find(DevProject.class, requirement.getProject().getId());
        if (null == project) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.project_not_found"));
        return project;
    }

    private void checkRepos(DevRequirement requirement, DevProject project) {
        if (null == requirement.getRepos() || requirement.getRepos().isEmpty()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.repos_required"));
        }
        Set<String> aliases = project.getRepos().stream().map(DevProjectRepo::getAlias).collect(Collectors.toSet());
        for (String alias : requirement.getRepos()) {
            if (!aliases.contains(alias)) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.repo_not_in_project") + ": " + alias);
            }
        }
    }

}
