package xyz.erupt.git.model;

/**
 * A worktree's state from {@code git status --porcelain=v2 --branch}.
 *
 * @param upstream  tracked branch, null when none
 * @param ahead     commits on the branch the upstream lacks
 * @param behind    commits on the upstream the branch lacks
 * @param changed   tracked files modified, staged or deleted
 * @param untracked files git does not track yet
 * @param conflicts files left unmerged
 * @author YuePeng
 * date 2026/10/21
 */
public record GitStatus(String head, String branch, String upstream, int ahead, int behind, int changed,
                        int untracked, int conflicts) {

    public boolean clean() {
        return changed == 0 && untracked == 0 && conflicts == 0;
    }

}
