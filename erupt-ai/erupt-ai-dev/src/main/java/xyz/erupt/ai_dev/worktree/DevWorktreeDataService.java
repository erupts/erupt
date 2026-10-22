package xyz.erupt.ai_dev.worktree;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.erupt.ai_dev.constant.RepoStatus;
import xyz.erupt.ai_dev.model.DevProject;
import xyz.erupt.ai_dev.model.DevProjectRepo;
import xyz.erupt.ai_dev.service.DevAccessService;
import xyz.erupt.ai_dev.service.DevLayout;
import xyz.erupt.ai_dev.service.DevRepoService;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.service.EruptBeanDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.git.GitException;
import xyz.erupt.git.GitRepo;
import xyz.erupt.git.model.GitStatus;
import xyz.erupt.git.model.GitWorktree;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lists the worktrees of the project in the page header straight from git.
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Slf4j
@Service
public class DevWorktreeDataService extends EruptBeanDataService<DevWorktree> {

    public static final String DATA_PROCESSOR = "AI_DEV_WORKTREE";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, DevWorktreeDataService.class);
    }

    @Resource
    private DevAccessService devAccessService;

    @Resource
    private DevRepoService devRepoService;

    @Resource
    private DevLayout devLayout;

    @Override
    protected List<DevWorktree> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        DevProject project = devAccessService.currentProject();
        Path root = real(devLayout.worktreeRoot(project.getTenantId(), project.getCode()));
        List<DevWorktree> list = new ArrayList<>();
        for (DevProjectRepo repo : project.getRepos()) {
            if (repo.getStatus() != RepoStatus.READY) continue;
            GitRepo gitRepo = devRepoService.open(project, repo.getAlias());
            if (null == gitRepo) continue;
            for (GitWorktree wt : gitRepo.worktrees()) {
                // only worktrees erupt made for this project: a bare clone may list others
                Path path = real(wt.path());
                if (wt.bare() || !path.startsWith(root) || root.relativize(path).getNameCount() != 2) continue;
                list.add(this.row(gitRepo, wt, path, root, repo.getAlias()));
            }
        }
        return list;
    }

    private DevWorktree row(GitRepo gitRepo, GitWorktree wt, Path path, Path root, String alias) {
        DevWorktree row = new DevWorktree();
        row.setPath(path.toString());
        row.setRequirement(root.relativize(path).getName(0).toString().toUpperCase());
        row.setAlias(alias);
        row.setBranch(wt.branch());
        row.setHead(null == wt.head() ? null : wt.head().substring(0, Math.min(8, wt.head().length())));
        row.setMissing(wt.prunable());
        if (!wt.prunable()) {
            try {
                GitStatus status = gitRepo.status(path);
                row.setUpstream(status.upstream());
                row.setAhead(status.ahead());
                row.setBehind(status.behind());
                row.setChanged(status.changed() + status.conflicts());
                row.setUntracked(status.untracked());
            } catch (GitException e) {
                log.warn("Cannot read status of worktree {}: {}", path, e.getMessage());
            }
        }
        return row;
    }

    /**
     * Removes the worktree's directory and registration. Uncommitted changes refuse the removal instead of
     * being discarded; the branch is kept.
     */
    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        DevProject project = devAccessService.currentProject();
        devAccessService.requireMember(project.getId());
        DevWorktree target = (DevWorktree) object;
        Path root = real(devLayout.worktreeRoot(project.getTenantId(), project.getCode()));
        Path path = Path.of(target.getPath()).toAbsolutePath().normalize();
        if (!path.startsWith(root)) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.worktree_outside_project"));
        GitRepo gitRepo = devRepoService.open(project, target.getAlias());
        if (null == gitRepo) throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.repo_not_synced"));
        try {
            if (Boolean.TRUE.equals(target.getMissing())) gitRepo.prune();
            else gitRepo.removeWorktree(path, false);
        } catch (GitException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("ai_dev.worktree_remove_failed") + ": " + e.getStderr().strip());
        }
    }

    // git reports resolved paths (/private/var on macOS for /var): compare like with like
    private static Path real(Path path) {
        try {
            return path.toRealPath();
        } catch (IOException e) {
            return path.toAbsolutePath().normalize();
        }
    }

}
