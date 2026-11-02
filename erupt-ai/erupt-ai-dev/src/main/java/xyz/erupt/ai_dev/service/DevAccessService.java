package xyz.erupt.ai_dev.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.ai_dev.constant.MemberRole;
import xyz.erupt.ai_dev.model.DevProject;
import xyz.erupt.ai_dev.model.DevRequirement;
import xyz.erupt.ai_dev.scope.DevProjectScope;
import xyz.erupt.core.module.MetaUserinfo;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.scope.EruptScopes;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.service.EruptUserService;

import java.util.List;

/**
 * Who may see and manage which project. Membership is the only gate: a super admin sees every project,
 * anyone else only the projects they are a member of.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Service
public class DevAccessService {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptUserService eruptUserService;

    public MetaUserinfo user() {
        return eruptUserService.getSimpleUserInfo();
    }

    public boolean superAdmin() {
        MetaUserinfo user = this.user();
        return null != user && user.isSuperAdmin();
    }

    /**
     * Ids of the projects the current user is a member of, every project for a super admin.
     */
    public List<Long> visibleProjectIds() {
        if (this.superAdmin()) {
            return eruptDao.getEntityManager().createQuery("select p.id from DevProject p", Long.class).getResultList();
        }
        MetaUserinfo user = this.user();
        if (null == user) return List.of();
        return eruptDao.getEntityManager()
                .createQuery("select distinct p.id from DevProject p join p.members m where m.user.id = :uid", Long.class)
                .setParameter("uid", user.getId()).getResultList();
    }

    /**
     * Ids of the projects the current user created, every project for a super admin: what the project
     * list shows. Membership decides what the pool shows.
     */
    public List<Long> ownProjectIds() {
        if (this.superAdmin()) {
            return eruptDao.getEntityManager().createQuery("select p.id from DevProject p", Long.class).getResultList();
        }
        MetaUserinfo user = this.user();
        if (null == user) return List.of();
        return eruptDao.getEntityManager()
                .createQuery("select p.id from DevProject p where p.creator.id = :uid", Long.class)
                .setParameter("uid", user.getId()).getResultList();
    }

    public List<DevProject> visibleProjects() {
        List<Long> ids = this.visibleProjectIds();
        if (ids.isEmpty()) return List.of();
        return eruptDao.lambdaQuery(DevProject.class).in(DevProject::getId, ids).list().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toList();
    }

    /**
     * The project in the page header, already checked against the user's options by the scope.
     */
    public DevProject currentProject() {
        DevProject project = eruptDao.find(DevProject.class, EruptScopes.get(DevProjectScope.class).getProject());
        if (null == project) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.project_not_found"));
        return project;
    }

    /**
     * Whether the current user may change the project's settings, repositories and members.
     */
    public boolean manages(Long projectId) {
        MemberRole role = this.role(projectId);
        return null != role && role.manages();
    }

    /**
     * The current user's role in the project, OWNER for a super admin, null for a non-member.
     */
    public MemberRole role(Long projectId) {
        if (this.superAdmin()) return MemberRole.OWNER;
        MetaUserinfo user = this.user();
        if (null == user || null == projectId) return null;
        List<MemberRole> roles = eruptDao.getEntityManager()
                .createQuery("select m.role from DevProject p join p.members m where p.id = :pid and m.user.id = :uid", MemberRole.class)
                .setParameter("pid", projectId).setParameter("uid", user.getId()).getResultList();
        return roles.isEmpty() ? null : roles.get(0);
    }

    /**
     * Approving a requirement or accepting its delivery: reviewers, owners and maintainers.
     */
    public void requireReviewer(Long projectId) {
        MemberRole role = this.role(projectId);
        if (null == role || !(role.manages() || role == MemberRole.REVIEWER)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.no_review_permission"));
        }
    }

    /**
     * Whether the current user wrote the requirement: by id, by name for rows from before ids were kept.
     */
    public boolean isCreator(DevRequirement r) {
        MetaUserinfo user = this.user();
        if (null == user) return false;
        if (null != r.getCreator()) return r.getCreator().getId().equals(user.getId());
        return null != r.getCreateBy() && r.getCreateBy().equals(user.getUsername());
    }

    /**
     * Approving or rejecting: a reviewer, and not the creator unless the project allows self review.
     */
    public boolean mayApprove(DevRequirement r) {
        MemberRole role = this.role(r.getProject().getId());
        if (null == role || !(role.manages() || role == MemberRole.REVIEWER)) return false;
        return Boolean.TRUE.equals(r.getProject().getSelfReview()) || !this.isCreator(r);
    }

    public void requireApprover(DevRequirement r) {
        this.requireReviewer(r.getProject().getId());
        if (!this.mayApprove(r)) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.self_review_forbidden"));
    }

    public void requireManage(Long projectId) {
        if (!this.manages(projectId)) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.no_manage_permission"));
    }

    public void requireMember(Long projectId) {
        if (!this.visibleProjectIds().contains(projectId)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.project_not_found"));
        }
    }

}
