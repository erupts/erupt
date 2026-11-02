package xyz.erupt.git;

import lombok.Getter;
import org.springframework.stereotype.Service;
import xyz.erupt.git.model.GitRemote;
import xyz.erupt.git.model.GitIdent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Entry point of erupt-git: opens or clones bare repositories.
 * <pre>
 * GitRepo repo = eruptGit.cloneBare(url, dir, GitAuth.token(token));
 * repo.addWorktree(wt, "req/req-1", "origin/develop");
 * repo.push(wt, auth, "req/req-1");
 * </pre>
 *
 * @author YuePeng
 * date 2026/10/21
 */
@Service
public class EruptGit {

    @Getter
    private final GitCli cli;

    private final GitIdent committer;

    public EruptGit(GitProp prop) {
        this.cli = new GitCli(prop);
        this.committer = new GitIdent(prop.getCommitterName(), prop.getCommitterEmail());
    }

    public String version() {
        return cli.run(null, "--version");
    }

    /**
     * Opens an existing bare repository.
     */
    public GitRepo open(Path dir) {
        Path repo = GitCli.path(dir);
        if (!"true".equals(cli.run(repo, "rev-parse", "--is-bare-repository"))) {
            throw new GitException("Not a bare repository: " + repo);
        }
        return new GitRepo(cli, repo, committer);
    }

    /**
     * Clones {@code url} into a new bare repository at {@code dir} and fetches it, so remote branches are
     * available as {@code origin/*}. A failed clone leaves nothing behind.
     */
    public GitRepo cloneBare(String url, Path dir, GitAuth auth) {
        Path repo = GitCli.path(dir);
        if (Files.exists(repo)) throw new GitException("Target already exists: " + repo);
        try {
            cli.run(null, auth, Map.of(), "clone", "--bare", "--", cli.url(url), repo.toString());
            // a bare clone maps remote heads onto its own heads and sets no fetch refspec; track them as
            // origin/* instead, so work branches and remote branches never collide
            cli.run(repo, "config", "remote.origin.fetch", "+refs/heads/*:refs/remotes/origin/*");
            GitRepo gitRepo = new GitRepo(cli, repo, committer);
            gitRepo.fetch(auth);
            return gitRepo;
        } catch (RuntimeException e) {
            delete(repo);
            throw e;
        }
    }

    /**
     * Checks that the remote is reachable with these credentials, without cloning.
     */
    public void verifyRemote(String url, GitAuth auth) {
        cli.run(null, auth, Map.of(), "ls-remote", "--heads", "--", cli.url(url));
    }

    /**
     * The remote's default branch and its branches, e.g. to pick or check a base branch before cloning.
     */
    public GitRemote remote(String url, GitAuth auth) {
        String checked = cli.url(url);
        String head = cli.run(null, auth, Map.of(), "ls-remote", "--symref", "--", checked, "HEAD");
        String defaultBranch = null;
        for (String line : head.split("\n")) {
            // ref: refs/heads/master\tHEAD
            if (line.startsWith("ref: refs/heads/")) defaultBranch = line.substring("ref: refs/heads/".length()).split("\t")[0].trim();
        }
        List<String> branches = new ArrayList<>();
        for (String line : cli.run(null, auth, Map.of(), "ls-remote", "--heads", "--", checked).split("\n")) {
            int at = line.indexOf("refs/heads/");
            if (at >= 0) branches.add(line.substring(at + "refs/heads/".length()).trim());
        }
        Collections.sort(branches);
        return new GitRemote(defaultBranch, branches);
    }

    private static void delete(Path dir) {
        if (!Files.exists(dir)) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(it -> it.toFile().delete());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
