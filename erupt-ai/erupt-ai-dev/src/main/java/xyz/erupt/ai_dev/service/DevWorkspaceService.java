package xyz.erupt.ai_dev.service;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.git.GitRepo;
import xyz.erupt.git.model.GitIdent;

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
