# erupt-git

Git capability for Erupt modules, built on the `git` CLI (2.20 or newer on the server's PATH). No tables, no pages: other modules (such as the planned erupt-ai-dev) drive it.

Works with any git host — GitHub, GitLab, Gitee, Gitea, Bitbucket or a plain git server — because it only speaks the git protocol. Hosting-platform features such as pull requests are deliberately out of scope.

## Model

A **bare repository** per remote, cloned once. Remote branches are tracked as `origin/*`; the repository's own `refs/heads` hold the work branches. **Worktrees** are checked out from it, one directory per branch.

```java
GitRepo repo = eruptGit.cloneBare("https://github.com/acme/web.git", Path.of("/data/repos/web.git"), GitAuth.token(token));

Path wt = Path.of("/data/worktrees/req-1/web");
repo.addWorktree(wt, "req/req-1", "origin/develop");
// ... edit files in wt ...
repo.commitAll(wt, new GitIdent("Claude Code", "agent@erupt.local"), "fix: align loading background");

// deliver to a branch of its own
repo.push(wt, auth, "req/req-1");

// or straight onto the base branch: rebase, then fast-forward only
repo.fetch(auth);
GitRebaseResult rebased = repo.rebase(wt, "origin/develop");   // conflicts are aborted and listed
GitPushResult pushed = repo.push(wt, auth, "develop");          // NON_FAST_FORWARD / REJECTED, never forced
```

| `GitRepo` | |
|---|---|
| `fetch` | `fetch --prune origin` |
| `worktrees` / `addWorktree` / `removeWorktree` / `prune` | worktree registry, parsed from `--porcelain` |
| `status` | branch, upstream, ahead/behind, changed / untracked / conflicted counts |
| `commitAll` | commits everything as the given author; `null` when nothing changed |
| `rebase` | aborts on conflict and answers the conflicting files |
| `push` | `PUSHED`, `UP_TO_DATE`, `NON_FAST_FORWARD` (remote moved on), `REJECTED` (e.g. protected branch) |
| `diff` / `changedFiles` / `commitsAhead` | what a branch adds since it forked from its base |

## Security

- Commands run as an argument array, never through a shell. Refs, URLs and paths are validated first, so a value cannot be read as an option (`-x`, `--upload-pack=…`) or a range.
- Remote URLs: `https`, `ssh` and scp-like (`git@host:org/repo.git`). Transport helpers (`ext::`, `fd::`) are refused, as is a URL carrying credentials. `file://` is refused unless `erupt.git.allow-local-remote` is on.
- Credentials (`GitAuth`) reach git only through the child process environment, answered by an inline credential helper. They never appear on a command line, in a URL, in `.git/config` or on disk. erupt-git stores none: the caller resolves them from its secret store.
- Repository hooks never run on the server (`core.hooksPath` points nowhere, commits use `--no-verify`).
- Prompts are disabled (`GIT_TERMINAL_PROMPT=0`, SSH `BatchMode`), so a missing credential fails fast instead of hanging. Every command is killed after `erupt.git.timeout-seconds`.

## Configuration

```yaml
erupt:
  git:
    binary: git                 # executable, from PATH unless absolute
    timeout-seconds: 600        # per command, clone included
    allow-local-remote: false   # allow file:// remotes
    committer-name: erupt       # committer of commits erupt writes or rebases
    committer-email: erupt@localhost
```

## Dependency

```xml
<dependency>
    <groupId>xyz.erupt</groupId>
    <artifactId>erupt-git</artifactId>
    <version>${erupt.version}</version>
</dependency>
```
