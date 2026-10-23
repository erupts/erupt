package xyz.erupt.ai_dev.proxy;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.ai_dev.constant.MemberRole;
import xyz.erupt.ai_dev.model.DevProject;
import xyz.erupt.ai_dev.model.DevProjectMember;
import xyz.erupt.ai_dev.model.DevProjectRepo;
import xyz.erupt.ai_dev.service.DevAccessService;
import xyz.erupt.ai_dev.service.DevRepoService;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.git.EruptGit;
import xyz.erupt.git.GitCli;
import xyz.erupt.git.GitException;
import xyz.erupt.git.model.GitRemote;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Keeps a project consistent and private: members only see their own projects, only owners and
 * maintainers change one, and every repository is checked before it is cloned.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Component
public class DevProjectProxy implements DataProxy<DevProject>, OperationHandler<DevProject, Void> {

    @Resource
    private DevAccessService devAccessService;

    @Resource
    private DevRepoService devRepoService;

    @Resource
    private EruptGit eruptGit;

    @Resource
    private EruptDao eruptDao;

    @Override
    public String beforeFetch(List<Condition> conditions) {
        if (!devAccessService.superAdmin()) {
            List<Long> ids = devAccessService.ownProjectIds();
            conditions.add(new Condition("id", ids.isEmpty() ? List.of(-1L) : ids, QueryExpression.IN));
        }
        return null;
    }

    // a single read by id skips beforeFetch: the detail and edit forms follow the list
    @Override
    public void editBehavior(DevProject project) {
        if (!devAccessService.ownProjectIds().contains(project.getId())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.project_not_found"));
        }
    }

    @Override
    public void beforeAdd(DevProject project) {
        this.check(project);
        // the creator owns what they create, so they keep access to it
        Long uid = devAccessService.user().getId();
        project.setCreator(eruptDao.find(EruptUser.class, uid));
        if (project.getMembers().stream().noneMatch(it -> null != it.getUser() && uid.equals(it.getUser().getId()))) {
            DevProjectMember owner = new DevProjectMember();
            owner.setUser(eruptDao.find(EruptUser.class, uid));
            owner.setRole(MemberRole.OWNER);
            project.getMembers().add(owner);
        }
        if (project.getMembers().stream().noneMatch(it -> it.getRole().manages())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.owner_required"));
        }
    }

    @Override
    public void beforeUpdate(DevProject project) {
        devAccessService.requireManage(project.getId());
        this.check(project);
        if (project.getMembers().stream().noneMatch(it -> it.getRole().manages())) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.owner_required"));
        }
    }

    @Override
    public void beforeDelete(DevProject project) {
        devAccessService.requireManage(project.getId());
        Long count = eruptDao.getEntityManager()
                .createQuery("select count(r) from DevRequirement r where r.project.id = :pid", Long.class)
                .setParameter("pid", project.getId()).getSingleResult();
        if (count > 0) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.project_has_requirements"));
        }
    }

    @Override
    public void afterAdd(DevProject project) {
        devRepoService.syncAfterCommit(project.getId());
    }

    @Override
    public void afterUpdate(DevProject project) {
        devRepoService.syncAfterCommit(project.getId());
    }

    // Row operation: sync repositories
    @Override
    public String exec(List<DevProject> data, Void form, String[] param) {
        for (DevProject project : data) {
            devAccessService.requireMember(project.getId());
            devRepoService.syncAfterCommit(project.getId());
        }
        return null;
    }

    // the base branch must exist on the remote, when it answers; blank takes the remote's default branch
    private void checkBaseBranch(DevProjectRepo repo) {
        GitRemote remote;
        try {
            remote = eruptGit.remote(repo.getUrl(), devRepoService.auth(repo.getCredential()));
        } catch (GitException e) {
            // a remote that is down for now does not block the save of a named branch; the run checks again
            if (StringUtils.isNotBlank(repo.getBaseBranch())) return;
            throw new EruptWebApiRuntimeException(repo.getAlias() + ": " + I18nTranslate.$translate("ai_dev.remote_unreachable") + " " + e.getMessage());
        }
        if (StringUtils.isBlank(repo.getBaseBranch())) {
            if (null == remote.defaultBranch()) {
                throw new EruptWebApiRuntimeException(repo.getAlias() + ": " + I18nTranslate.$translate("ai_dev.remote_empty"));
            }
            repo.setBaseBranch(remote.defaultBranch());
        } else if (!remote.branches().contains(repo.getBaseBranch().trim())) {
            throw new EruptWebApiRuntimeException(repo.getAlias() + ": " + I18nTranslate.$translate("ai_dev.base_branch_missing")
                    .replace("{0}", repo.getBaseBranch()).replace("{1}", branchHint(remote)));
        }
    }

    // the default branch first, then a handful of the others
    private static String branchHint(GitRemote remote) {
        List<String> names = new ArrayList<>();
        if (null != remote.defaultBranch()) names.add(remote.defaultBranch());
        remote.branches().stream().filter(it -> !it.equals(remote.defaultBranch())).limit(9).forEach(names::add);
        return String.join(", ", names) + (remote.branches().size() > names.size() ? ", …" : "");
    }

    private void check(DevProject project) {
        if (null == project.getCode() || !project.getCode().matches(DevProject.CODE_REGEX)) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.code_invalid"));
        }
        Set<String> aliases = new HashSet<>();
        for (DevProjectRepo repo : project.getRepos()) {
            if (null == repo.getAlias() || !repo.getAlias().matches(DevProjectRepo.ALIAS_REGEX)) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.alias_invalid") + ": " + repo.getAlias());
            }
            if (!aliases.add(repo.getAlias())) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.alias_duplicate") + ": " + repo.getAlias());
            }
            try {
                eruptGit.getCli().url(repo.getUrl());
                if (StringUtils.isNotBlank(repo.getBaseBranch())) GitCli.ref(repo.getBaseBranch().trim());
                if (StringUtils.isNotBlank(repo.getTargetBranch())) GitCli.ref(repo.getTargetBranch());
            } catch (GitException e) {
                throw new EruptWebApiRuntimeException(repo.getAlias() + ": " + e.getMessage());
            }
            if (StringUtils.isNotBlank(repo.getCredential()) && !devRepoService.credentialExists(repo.getCredential())) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.credential_unknown") + ": " + repo.getCredential());
            }
            this.checkBaseBranch(repo);
        }
        Set<Long> users = new HashSet<>();
        for (DevProjectMember member : project.getMembers()) {
            Long uid = null == member.getUser() ? null : member.getUser().getId();
            if (null != uid && !users.add(uid)) {
                throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.member_duplicate") + ": " + member.getUser().getName());
            }
        }
    }

}
