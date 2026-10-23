package xyz.erupt.ai_dev.service;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.git.model.GitStatus;
import org.springframework.stereotype.Service;
import xyz.erupt.git.GitRepo;
import xyz.erupt.git.model.GitIdent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A requirement's workspace: one directory holding a worktree of every repository it touches, plus the
 * project's AGENTS.md. Claude Code reads CLAUDE.md, Codex reads AGENTS.md, so both are written, the first
 * importing the second.
 *
 * @author YuePeng
 * date 2026/10/22
 */
@Service
public class DevWorkspaceService {

    @Resource
    private DevLayout devLayout;

    @Resource
    private DevRepoService devRepoService;

    /**
     * Brings every repository up to date and checks out the requirement's branch in its worktree; an
     * existing worktree (a retry) is kept as it is.
     */
    public Path prepare(DevRunContext ctx) throws IOException {
        Path workspace = devLayout.workspace(ctx.tenant(), ctx.projectCode(), ctx.code());
        Files.createDirectories(workspace);
        for (DevRunContext.Repo repo : ctx.repos()) {
            GitRepo gitRepo = devRepoService.fetch(ctx.tenant(), ctx.projectCode(), repo.alias(), repo.credential());
            Path wt = devLayout.worktreeDir(ctx.tenant(), ctx.projectCode(), ctx.code(), repo.alias());
            if (!Files.exists(wt)) {
                if (!gitRepo.hasRemoteBranch(repo.baseBranch())) {
                    throw new IllegalStateException(repo.alias() + ": base branch " + repo.baseBranch() + " is not on the remote, which has "
                            + String.join(", ", gitRepo.remoteBranches().stream().limit(10).toList()) + ". Fix it in the project's repositories.");
                }
                gitRepo.prune();
                gitRepo.addWorktree(wt, ctx.workBranch(), "origin/" + repo.baseBranch());
            }
        }
        if (StringUtils.isNotBlank(ctx.agentsMd())) {
            Files.writeString(workspace.resolve("AGENTS.md"), ctx.agentsMd(), StandardCharsets.UTF_8);
            Files.writeString(workspace.resolve("CLAUDE.md"), "@AGENTS.md\n", StandardCharsets.UTF_8);
        }
        return workspace;
    }

    /**
     * Commits whatever the agent left uncommitted, as the agent, so nothing it did is lost before review.
     */
    public void commitLeftovers(DevRunContext ctx, String agentName) {
        GitIdent author = new GitIdent(agentName, "agent@erupt.local");
        for (DevRunContext.Repo repo : ctx.repos()) {
            GitRepo gitRepo = devRepoService.open(ctx.tenant(), ctx.projectCode(), repo.alias());
            gitRepo.commitAll(this.worktree(ctx, repo), author,
                    ctx.code() + ": " + ctx.title() + "\n\nUncommitted changes left by the agent.\n\nRequirement: " + ctx.code());
        }
    }

    public Path worktree(DevRunContext ctx, DevRunContext.Repo repo) {
        return devLayout.worktreeDir(ctx.tenant(), ctx.projectCode(), ctx.code(), repo.alias());
    }

    /**
     * Removes the requirement's worktrees and workspace; the branches stay.
     */
    /**
     * One repository of a requirement's workspace, as it stands on the server.
     *
     * @param exists  whether the worktree is there; false once delivered, discarded or not yet run
     * @param ahead   commits on the branch beyond the latest base, -1 when unknown
     * @param changed files changed or added and not committed
     */
    public record Workspace(String alias, String branch, String base, boolean exists, int ahead, int changed, String path) {
    }

    public List<Workspace> describe(DevRunContext ctx) {
        List<Workspace> list = new ArrayList<>();
        for (DevRunContext.Repo repo : ctx.repos()) {
            Path wt = this.worktree(ctx, repo);
            if (!Files.exists(wt)) {
                list.add(new Workspace(repo.alias(), ctx.workBranch(), repo.baseBranch(), false, 0, 0, wt.toString()));
                continue;
            }
            int ahead = -1, changed = 0;
            try {
                GitRepo gitRepo = devRepoService.open(ctx.tenant(), ctx.projectCode(), repo.alias());
                GitStatus status = gitRepo.status(wt);
                changed = status.changed() + status.untracked();
                ahead = gitRepo.commitsAhead("origin/" + repo.baseBranch(), ctx.workBranch());
            } catch (RuntimeException e) {
                // a half-removed or foreign worktree still shows, with what could be read
            }
            list.add(new Workspace(repo.alias(), ctx.workBranch(), repo.baseBranch(), true, ahead, changed, wt.toString()));
        }
        return list;
    }

    /**
     * Throws a requirement's work away: its worktrees, uncommitted changes included, its local branch and its
     * workspace directory. The next run starts from the latest base again. Nothing pushed is touched.
     */
    public void discard(DevRunContext ctx) throws IOException {
        for (DevRunContext.Repo repo : ctx.repos()) {
            GitRepo gitRepo;
            try {
                gitRepo = devRepoService.open(ctx.tenant(), ctx.projectCode(), repo.alias());
            } catch (RuntimeException e) {
                continue;
            }
            Path wt = this.worktree(ctx, repo);
            if (Files.exists(wt)) gitRepo.removeWorktree(wt, true);
            gitRepo.prune();
            if (gitRepo.hasBranch(ctx.workBranch())) gitRepo.deleteBranch(ctx.workBranch());
        }
        Path workspace = devLayout.workspace(ctx.tenant(), ctx.projectCode(), ctx.code());
        if (Files.exists(workspace)) {
            try (var paths = Files.walk(workspace)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
    }

    /**
     * A workspace left by a requirement that no longer exists: its worktrees are dropped from every
     * repository of the project, then the directory goes.
     */
    public void discardDirectory(String tenant, String projectCode, String dir) throws IOException {
        Path workspace = devLayout.worktreeRoot(tenant, projectCode).resolve(dir).normalize();
        // only a direct child of the project's worktree root
        if (!workspace.getParent().equals(devLayout.worktreeRoot(tenant, projectCode))) throw new IOException("Not a workspace: " + dir);
        try (var paths = Files.walk(workspace)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
        }
        Path repos = devLayout.repoDir(tenant, projectCode, "x").getParent();
        if (Files.isDirectory(repos)) {
            try (var bare = Files.list(repos)) {
                for (Path repo : bare.filter(Files::isDirectory).toList()) {
                    try {
                        devRepoService.open(tenant, projectCode, repo.getFileName().toString().replaceFirst("\\.git$", "")).prune();
                    } catch (RuntimeException e) {
                        // a repository not cloned yet has nothing to prune
                    }
                }
            }
        }
    }

    // requirement workspaces of a project still on the server
    public List<String> workspaces(String tenant, String projectCode) {
        Path root = devLayout.worktreeRoot(tenant, projectCode);
        if (!Files.isDirectory(root)) return List.of();
        try (var dirs = Files.list(root)) {
            return dirs.filter(Files::isDirectory).map(it -> it.getFileName().toString()).sorted().toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    public void cleanUp(DevRunContext ctx) throws IOException {
        for (DevRunContext.Repo repo : ctx.repos()) {
            Path wt = this.worktree(ctx, repo);
            if (Files.exists(wt)) devRepoService.open(ctx.tenant(), ctx.projectCode(), repo.alias()).removeWorktree(wt, false);
        }
        Path workspace = devLayout.workspace(ctx.tenant(), ctx.projectCode(), ctx.code());
        Files.deleteIfExists(workspace.resolve("AGENTS.md"));
        Files.deleteIfExists(workspace.resolve("CLAUDE.md"));
        try (var rest = Files.list(workspace)) {
            if (rest.findAny().isEmpty()) Files.delete(workspace);
        }
    }

}
