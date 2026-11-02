package xyz.erupt.git.model;

import java.util.List;

/**
 * Outcome of a rebase. A conflicting rebase is aborted before this is returned, so the worktree is left
 * exactly as it was.
 *
 * @param conflicts files that conflicted, empty on success
 * @author YuePeng
 * date 2026/10/21
 */
public record GitRebaseResult(boolean ok, List<String> conflicts) {
}
