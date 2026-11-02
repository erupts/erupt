package xyz.erupt.git;

import lombok.Getter;
import xyz.erupt.git.model.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A bare repository and the worktrees hung off it. Remote branches are read as {@code origin/<name>};
 * the repository's own {@code refs/heads} hold the work branches its worktrees check out.
 *
 * @author YuePeng
 * date 2026/10/21
 */
public class GitRepo {

    private static final String ORIGIN = "origin";

    private final GitCli cli;

    @Getter
    private final Path dir;

    private final GitIdent committer;

    GitRepo(GitCli cli, Path dir, GitIdent committer) {
        this.cli = cli;
        this.dir = dir;
        this.committer = committer;
    }

    // ------------------------------------------------------------------ remote

    /**
     * Brings every {@code origin/*} up to date, dropping branches deleted on the remote.
     */
    public void fetch(GitAuth auth) {
        cli.run(dir, auth, Map.of(), "fetch", "--prune", ORIGIN);
    }

    public String originUrl() {
        return cli.run(dir, "remote", "get-url", ORIGIN);
    }

    /**
     * Points {@code origin} at a new URL, e.g. after the repository moved hosts.
     */
    public void setOriginUrl(String url) {
        cli.run(dir, "remote", "set-url", ORIGIN, cli.url(url));
    }

    /**
     * Pushes the worktree's HEAD to {@code branch} on the remote, fast-forward only: a push that would lose
     * remote commits comes back {@link GitPushResult.Status#NON_FAST_FORWARD}, never forced.
     */
    public GitPushResult push(Path worktree, GitAuth auth, String branch) {
        GitCli.Result result = cli.exec(GitCli.path(worktree), auth, Map.of(),
                "push", "--porcelain", ORIGIN, "HEAD:refs/heads/" + GitCli.ref(branch));
        for (String line : result.lines()) {
            String[] cols = line.split("\t");
            if (cols.length < 3 || line.isEmpty()) continue;
            String detail = cols[2];
            return switch (line.charAt(0)) {
                case '=' -> new GitPushResult(GitPushResult.Status.UP_TO_DATE, detail);
                case '!' -> new GitPushResult(detail.contains("remote rejected")
                        ? GitPushResult.Status.REJECTED : GitPushResult.Status.NON_FAST_FORWARD, detail);
                default -> new GitPushResult(GitPushResult.Status.PUSHED, detail);
            };
        }
        throw new GitException("git push failed", result.exitCode(), result.stderr());
    }

    // ------------------------------------------------------------------ worktrees

    public List<GitWorktree> worktrees() {
        List<GitWorktree> list = new ArrayList<>();
        Map<String, String> entry = new HashMap<>();
        for (String line : (cli.run(dir, "worktree", "list", "--porcelain") + "\n").split("\n", -1)) {
            if (!line.isEmpty()) {
                int space = line.indexOf(' ');
                entry.put(space < 0 ? line : line.substring(0, space), space < 0 ? "" : line.substring(space + 1));
                continue;
            }
            if (entry.containsKey("worktree")) {
                String branch = entry.get("branch");
                list.add(new GitWorktree(Path.of(entry.get("worktree")), entry.get("HEAD"),
                        null == branch ? null : branch.replaceFirst("^refs/heads/", ""),
                        entry.containsKey("bare"), entry.containsKey("detached"),
                        entry.containsKey("locked"), entry.containsKey("prunable")));
            }
            entry.clear();
        }
        return list;
    }

    /**
     * Checks {@code branch} out at {@code path}; a branch that does not exist yet is created from
     * {@code startPoint}, e.g. {@code origin/develop}.
     */
    public void addWorktree(Path path, String branch, String startPoint) {
        Path target = GitCli.path(path);
        if (this.hasBranch(branch)) {
            cli.run(dir, "worktree", "add", target.toString(), GitCli.ref(branch));
        } else {
            cli.run(dir, "worktree", "add", "-b", GitCli.ref(branch), target.toString(), GitCli.ref(startPoint));
        }
    }

    /**
     * Removes the worktree's directory and registration; {@code force} also discards uncommitted changes.
     * The branch stays.
     */
    public void removeWorktree(Path path, boolean force) {
        String target = GitCli.path(path).toString();
        if (force) cli.run(dir, "worktree", "remove", "--force", target);
        else cli.run(dir, "worktree", "remove", target);
    }

    /**
     * Drops registrations whose directory was deleted by hand.
     */
    public void prune() {
        cli.run(dir, "worktree", "prune");
    }

    public void deleteBranch(String branch) {
        cli.run(dir, "branch", "-D", GitCli.ref(branch));
    }

    public boolean hasBranch(String branch) {
        return cli.exec(dir, GitAuth.NONE, Map.of(), "show-ref", "--verify", "--quiet", "refs/heads/" + GitCli.ref(branch)).ok();
    }

    /**
     * Whether the remote had {@code branch} at the last fetch.
     */
    // branch names under origin, as last fetched
    public List<String> remoteBranches() {
        List<String> branches = new ArrayList<>();
        String prefix = "refs/remotes/" + ORIGIN + "/";
        for (String ref : cli.run(dir, "for-each-ref", "--format=%(refname)", prefix).split("\n")) {
            if (ref.startsWith(prefix) && !ref.equals(prefix + "HEAD")) branches.add(ref.substring(prefix.length()));
        }
        return branches;
    }

    public boolean hasRemoteBranch(String branch) {
        return cli.exec(dir, GitAuth.NONE, Map.of(), "show-ref", "--verify", "--quiet", "refs/remotes/" + ORIGIN + "/" + GitCli.ref(branch)).ok();
    }

    // ------------------------------------------------------------------ inside a worktree

    public GitStatus status(Path worktree) {
        String head = null, branch = null, upstream = null;
        int ahead = 0, behind = 0, changed = 0, untracked = 0, conflicts = 0;
        for (String line : cli.run(GitCli.path(worktree), "status", "--porcelain=v2", "--branch").split("\n")) {
            if (line.startsWith("# branch.oid ")) head = line.substring(13);
            else if (line.startsWith("# branch.head ")) branch = line.substring(14);
            else if (line.startsWith("# branch.upstream ")) upstream = line.substring(18);
            else if (line.startsWith("# branch.ab ")) {
                String[] ab = line.substring(12).split(" ");
                ahead = Integer.parseInt(ab[0].substring(1));
                behind = Integer.parseInt(ab[1].substring(1));
            } else if (line.startsWith("? ")) untracked++;
            else if (line.startsWith("u ")) conflicts++;
            else if (line.startsWith("1 ") || line.startsWith("2 ")) changed++;
        }
        if ("(detached)".equals(branch)) branch = null;
        return new GitStatus(head, branch, upstream, ahead, behind, changed, untracked, conflicts);
    }

    /**
     * Commits everything in the worktree; answers the new commit id, or null when there was nothing to commit.
     */
    public String commitAll(Path worktree, GitIdent author, String message) {
        Path wt = GitCli.path(worktree);
        cli.run(wt, "add", "--all");
        if (cli.exec(wt, GitAuth.NONE, Map.of(), "diff", "--cached", "--quiet").ok()) return null;
        Map<String, String> env = new HashMap<>(author.authorEnv());
        env.putAll(committer.committerEnv());
        cli.run(wt, GitAuth.NONE, env, "commit", "--no-verify", "-m", message);
        return this.head(wt);
    }

    /**
     * Replays the worktree's commits onto {@code onto}. On conflict the rebase is aborted and the
     * conflicting files are answered, leaving the worktree as it was.
     */
    public GitRebaseResult rebase(Path worktree, String onto) {
        Path wt = GitCli.path(worktree);
        GitCli.Result result = cli.exec(wt, GitAuth.NONE, committer.committerEnv(), "rebase", GitCli.ref(onto));
        if (result.ok()) return new GitRebaseResult(true, List.of());
        String unmerged = cli.run(wt, "diff", "--name-only", "--diff-filter=U");
        if (unmerged.isEmpty()) throw new GitException("git rebase failed", result.exitCode(), result.stderr());
        cli.run(wt, "rebase", "--abort");
        return new GitRebaseResult(false, List.of(unmerged.split("\n")));
    }

    public String head(Path worktree) {
        return cli.run(GitCli.path(worktree), "rev-parse", "HEAD");
    }

    // ------------------------------------------------------------------ comparing

    /**
     * Commits reachable from {@code head} but not from {@code base}.
     */
    public int commitsAhead(String base, String head) {
        return Integer.parseInt(cli.run(dir, "rev-list", "--count", GitCli.ref(base) + ".." + GitCli.ref(head)));
    }

    /**
     * Files {@code head} changed since it forked from {@code base}.
     */
    public List<String> changedFiles(String base, String head) {
        String out = cli.run(dir, "diff", "--name-only", GitCli.ref(base) + "..." + GitCli.ref(head));
        return out.isEmpty() ? List.of() : List.of(out.split("\n"));
    }

    /**
     * The patch {@code head} adds on top of where it forked from {@code base}: what a reviewer reads.
     */
    public String diff(String base, String head) {
        return cli.run(dir, "diff", GitCli.ref(base) + "..." + GitCli.ref(head));
    }

}
