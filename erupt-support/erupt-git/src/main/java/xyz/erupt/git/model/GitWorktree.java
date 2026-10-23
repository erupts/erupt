package xyz.erupt.git.model;

import java.nio.file.Path;

/**
 * One entry of {@code git worktree list --porcelain}.
 *
 * @param branch   short branch name, null when detached or bare
 * @param prunable its directory is gone; {@code git worktree prune} would drop it
 * @author YuePeng
 * date 2026/10/21
 */
public record GitWorktree(Path path, String head, String branch, boolean bare, boolean detached, boolean locked,
                          boolean prunable) {
}
