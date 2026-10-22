package xyz.erupt.git;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.erupt.git.model.GitIdent;
import xyz.erupt.git.model.GitRemote;
import xyz.erupt.git.model.GitPushResult;
import xyz.erupt.git.model.GitRebaseResult;
import xyz.erupt.git.model.GitStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs against a real git: a local bare "remote" seeded through a plain clone, reached over file://.
 *
 * @author YuePeng
 */
class GitRepoTest {

    private static final GitIdent AGENT = new GitIdent("Agent", "agent@test");

    private static final Map<String, String> IDENT = Map.of(
            "GIT_AUTHOR_NAME", "Seed", "GIT_AUTHOR_EMAIL", "seed@test",
            "GIT_COMMITTER_NAME", "Seed", "GIT_COMMITTER_EMAIL", "seed@test");

    @TempDir
    Path tmp;

    private EruptGit git;

    private GitCli cli;

    private Path seed;

    private String remoteUrl;

    @BeforeEach
    void setUp() throws IOException {
        GitProp prop = new GitProp();
        prop.setAllowLocalRemote(true);
        prop.setTimeoutSeconds(60);
        git = new EruptGit(prop);
        cli = git.getCli();
        try {
            git.version();
        } catch (GitException e) {
            Assumptions.abort("git is not installed");
        }
        Path remote = tmp.resolve("remote.git");
        cli.run(null, "init", "--bare", "--initial-branch=develop", remote.toString());
        remoteUrl = remote.toUri().toString();
        seed = tmp.resolve("seed");
        cli.run(null, "clone", remoteUrl, seed.toString());
        cli.run(seed, "checkout", "-b", "develop");
        seedCommit("app.txt", "line 1\n", "init");
    }

    private void seedCommit(String file, String content, String message) throws IOException {
        Files.writeString(seed.resolve(file), content);
        cli.run(seed, "add", "--all");
        cli.run(seed, GitAuth.NONE, IDENT, "commit", "-m", message);
        cli.run(seed, "push", "origin", "develop");
    }

    private GitRepo cloned() {
        return git.cloneBare(remoteUrl, tmp.resolve("repos/web.git"), GitAuth.NONE);
    }

    @Test
    void cloneBareTracksRemoteBranchesAsOrigin() {
        GitRepo repo = cloned();
        assertEquals(cli.run(seed, "rev-parse", "HEAD"), cli.run(repo.getDir(), "rev-parse", "origin/develop"));
        assertEquals(repo.getDir(), git.open(repo.getDir()).getDir());
    }

    @Test
    void remoteNamesItsDefaultBranchAndItsBranches() {
        cli.run(seed, "push", "origin", "develop:release");
        GitRemote remote = git.remote(remoteUrl, GitAuth.NONE);
        assertEquals("develop", remote.defaultBranch());
        assertEquals(List.of("develop", "release"), remote.branches());
        GitRepo repo = cloned();
        assertEquals(List.of("develop", "release"), repo.remoteBranches());
        assertFalse(repo.hasRemoteBranch("main"), "no main: the base branch must be checked, not assumed");
    }

    @Test
    void failedCloneLeavesNothingBehind() {
        Path target = tmp.resolve("repos/missing.git");
        assertThrows(GitException.class, () -> git.cloneBare(tmp.resolve("nope.git").toUri().toString(), target, GitAuth.NONE));
        assertFalse(Files.exists(target));
    }

    @Test
    void worktreeLifecycle() throws IOException {
        GitRepo repo = cloned();
        Path wt = tmp.resolve("worktrees/req-1/web");
        repo.addWorktree(wt, "req/req-1", "origin/develop");

        assertTrue(repo.worktrees().stream().anyMatch(it -> "req/req-1".equals(it.branch()) && same(it.path(), wt)));
        GitStatus status = repo.status(wt);
        assertTrue(status.clean());
        assertEquals("req/req-1", status.branch());
        assertEquals("origin/develop", status.upstream());

        Files.writeString(wt.resolve("feature.txt"), "x\n");
        assertEquals(1, repo.status(wt).untracked());
        String sha = repo.commitAll(wt, AGENT, "feat: add feature");
        assertNotNull(sha);
        assertNull(repo.commitAll(wt, AGENT, "nothing"));
        assertEquals(1, repo.status(wt).ahead());
        assertEquals(1, repo.commitsAhead("origin/develop", "req/req-1"));
        assertEquals(List.of("feature.txt"), repo.changedFiles("origin/develop", "req/req-1"));
        assertTrue(repo.diff("origin/develop", "req/req-1").contains("+x"));
        assertEquals("Agent", cli.run(wt, "log", "-1", "--format=%an"));

        assertEquals(GitPushResult.Status.PUSHED, repo.push(wt, GitAuth.NONE, "req/req-1").status());
        assertEquals(GitPushResult.Status.UP_TO_DATE, repo.push(wt, GitAuth.NONE, "req/req-1").status());
        assertEquals(sha, cli.run(tmp.resolve("remote.git"), "rev-parse", "req/req-1"));

        repo.removeWorktree(wt, false);
        assertFalse(Files.exists(wt));
        assertTrue(repo.worktrees().stream().noneMatch(it -> "req/req-1".equals(it.branch())));
        // the branch survives its worktree and is picked up again
        repo.addWorktree(wt, "req/req-1", "origin/develop");
        assertEquals(sha, repo.head(wt));
    }

    @Test
    void pushOntoMovedBaseIsRefusedUntilRebased() throws IOException {
        GitRepo repo = cloned();
        Path wt = tmp.resolve("worktrees/req-2/web");
        repo.addWorktree(wt, "req/req-2", "origin/develop");
        Files.writeString(wt.resolve("feature.txt"), "x\n");
        repo.commitAll(wt, AGENT, "feat: add feature");
        seedCommit("other.txt", "y\n", "someone else");

        GitPushResult refused = repo.push(wt, GitAuth.NONE, "develop");
        assertEquals(GitPushResult.Status.NON_FAST_FORWARD, refused.status());
        assertFalse(refused.ok());

        repo.fetch(GitAuth.NONE);
        assertTrue(repo.rebase(wt, "origin/develop").ok());
        assertTrue(repo.push(wt, GitAuth.NONE, "develop").ok());
        assertEquals(repo.head(wt), cli.run(tmp.resolve("remote.git"), "rev-parse", "develop"));
    }

    @Test
    void conflictingRebaseIsAborted() throws IOException {
        GitRepo repo = cloned();
        Path wt = tmp.resolve("worktrees/req-3/web");
        repo.addWorktree(wt, "req/req-3", "origin/develop");
        String before = repo.head(wt);
        Files.writeString(wt.resolve("app.txt"), "agent\n");
        repo.commitAll(wt, AGENT, "agent edit");
        seedCommit("app.txt", "human\n", "human edit");
        repo.fetch(GitAuth.NONE);

        GitRebaseResult result = repo.rebase(wt, "origin/develop");
        assertFalse(result.ok());
        assertEquals(List.of("app.txt"), result.conflicts());
        assertTrue(repo.status(wt).clean());
        assertNotEquals(before, repo.head(wt));
        assertEquals("agent\n", Files.readString(wt.resolve("app.txt")));
    }

    @Test
    void prunesWorktreeDeletedByHand() throws IOException {
        GitRepo repo = cloned();
        Path wt = tmp.resolve("worktrees/req-4/web");
        repo.addWorktree(wt, "req/req-4", "origin/develop");
        try (var walk = Files.walk(wt)) {
            walk.sorted(java.util.Comparator.reverseOrder()).forEach(it -> it.toFile().delete());
        }
        assertTrue(repo.worktrees().stream().anyMatch(it -> "req/req-4".equals(it.branch()) && it.prunable()));
        repo.prune();
        assertTrue(repo.worktrees().stream().noneMatch(it -> "req/req-4".equals(it.branch())));
    }

    @Test
    void refusesUnsafeArguments() {
        for (String ref : List.of("-x", "--upload-pack=sh", "a..b", "a//b", "a/", "a.lock", "a b", "@{1}")) {
            assertThrows(GitException.class, () -> GitCli.ref(ref), ref);
        }
        assertEquals("origin/develop", GitCli.ref("origin/develop"));
        for (String url : List.of("ext::sh -c touch% /tmp/x", "-uhttps://h/r.git", "https://user:token@host/r.git", "/etc", "fd::3")) {
            assertThrows(GitException.class, () -> cli.url(url), url);
        }
        assertThrows(GitException.class, () -> new GitCli(new GitProp()).url("file:///tmp/r.git"));
        assertEquals("git@github.com:org/repo.git", cli.url("git@github.com:org/repo.git"));
        assertThrows(GitException.class, () -> GitCli.path(Path.of("relative")));
        assertThrows(GitException.class, () -> new GitIdent("a\nb", "x@y"));
        assertThrows(GitException.class, () -> GitAuth.token("t\nprotocol=evil"));
        assertFalse(GitAuth.token("s3cret").toString().contains("s3cret"));
    }

    private static boolean same(Path a, Path b) {
        try {
            return a.toRealPath().equals(b.toRealPath());
        } catch (IOException e) {
            return false;
        }
    }

}
