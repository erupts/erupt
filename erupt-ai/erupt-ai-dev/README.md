# erupt-ai-dev

AI software delivery for Erupt: projects that span several git repositories, a requirement pool, and per-requirement worktrees that coding agents work in. Execution is server-managed; git goes through [`erupt-git`](../../erupt-support/erupt-git), so any git host works.

> Status: data model, project scope, repository sync, worktree view, agent execution (Claude Code) with MCP progress reporting, push delivery, and the requirement-pool page.

## Requirement pool page

The **AI Dev → Requirement Pool** menu opens `#/ai-dev/pool` in erupt-web. It has three parts:

- **Requirement list**: filter by stage, search by code or title.
- **Detail pane**:
  - stage steps and the actions the user may take,
  - live progress and the QA verdict,
  - the agent log, followed live,
  - the diff of every repository against its base,
  - the delivery records.

The page uses the same project as the page header (`DevProjectScope`). Reads go through `/erupt-api/ai-dev/pool/*`, which is guarded by the page's menu and by project membership. Writes go through the `DevRequirement` model's own add API and row operations, so validation, permissions and the operation log stay erupt's own. Agent output is shown as text only, never rendered as HTML.

## Lifecycle

```
DRAFT ─submit─▶ REVIEW ─approve─▶ QUEUED ─▶ RUNNING ─▶ VERIFYING ─▶ ACCEPTANCE ─accept & push─▶ DELIVERED
                  │reject                     │ stop / error            │ request changes ─▶ QUEUED
                  ▼                           ▼
                DRAFT                       FAILED ─retry─▶ QUEUED
```

Row actions on the requirement table drive it. Members submit, retry, stop and cancel; reviewers, owners and maintainers approve, reject, request changes and accept. The definition is frozen while queued, running or delivered.

## Execution

1. The executor polls the queue and claims a requirement with a conditional update, so two instances never take the same one.
2. **Workspace**: each repository is fetched and a worktree on `req/req-n` is checked out from `origin/<base>`. The project's AGENTS.md is written to the workspace root, with a CLAUDE.md that imports it.
3. **Developer agent**: the requirement's executor, a coding CLI picked when it is created (the picker offers the ones installed on the server, starting with the one the author used last), runs in the workspace with the prompt on stdin. Every message (text, tool call, tool result, final answer) is stored as an `Agent Event` as it arrives. The agent reports progress through erupt's MCP endpoint (below); a `PROGRESS <n> <phase>` line works too, for CLIs without MCP. A retry or a change request resumes the previous CLI session (`--resume`) and passes the new instruction.
4. Anything the agent left uncommitted is committed in its name.
5. **QA review** (optional, per requirement): a second run of the same executor checks the change and reports a verdict through MCP `devReportVerdict`. Without it, a `VERDICT: PASS | FAIL <reason>` line is used.
6. **ACCEPTANCE**: a human reviews, then *Accept and Push* delivers.
   - A repository with a target branch is rebased onto it and pushed fast-forward only.
   - A repository without one gets `req/req-n` pushed.
   - All repositories are rebased before the first push, so a conflict leaves every repository untouched.
   - Each push is recorded as a `Delivery`. The worktrees are removed after success.

### MCP endpoint


Each run reaches erupt-ai's MCP endpoint at `/mcp` with a token of its own: `DevMcpGrants` is an `McpAuthenticator`, so the token works whether or not `erupt.ai.mcp.server-enabled` is on, and it lists and calls only the tools of its role.

- **Token**: the run is given a random bearer token, minted for that run and revoked when it ends.
- **Config file**: the agent's `--mcp-config` file is written under `{root}/run/` with mode 0600 and deleted after the run.
- **Isolation**: `--strict-mcp-config` keeps the account's other MCP servers out of the run.
- **Tools**: `DevTools`, an `@AiToolbox(exposed = false)` bean, so chat agents and user tokens never see them: `devRequirement` for both roles; `devReportProgress` for the developer; `devReportVerdict` for QA. Next to them the run gets erupt's own tools (`@AiToolbox`), run as the requirement's creator in their tenant and limited to what that creator's AI roles allow in AI chat: the agent can read erupt models and their data the way the creator could, no further. What it reads goes to the model provider with the rest of its context.
- **Reach**: a token touches only its own requirement.
- **Address**: the agent reaches the endpoint at `http://127.0.0.1:<port>`. When the agent runs elsewhere, a container for example, set `executor.mcp-url`.

Follow a session live with `GET /erupt-api/ai-dev/session/{id}/events?after={seq}`.

### Coding CLIs

An agent's executor is a coding CLI on the server. The picker offers only the ones found there: the server's `PATH` (or a configured absolute path) is scanned, and a CLI installed later shows up within half a minute.

| Code | CLI | Headless mode | Session resume | MCP |
|---|---|---|---|---|
| `claude` | Claude Code | `claude -p --output-format stream-json` | yes | `--mcp-config` file |
| `codex` | Codex | `codex exec --json` | yes | `-c mcp_servers.erupt.*`, token in the environment |
| `opencode` | OpenCode | `opencode run --format json --standalone` | yes | `OPENCODE_CONFIG_CONTENT` |
| `kimi` | Kimi Code | `kimi --print --output-format stream-json` | yes | `--mcp-config-file` |
| `cursor` | Cursor CLI | `cursor-agent -p --output-format stream-json --force`, prompt as the last argument | yes | no: marker lines |
| `qwen` | Qwen Code | `qwen --output-format stream-json --yolo` | yes | no: marker lines |
| *any key* | a plain CLI | prompt on stdin (or as the last argument), text out | no | no: marker lines |

Claude Code, Cursor CLI and Qwen Code print the same stream-json, read by one parser (`StreamJsonRunner`).

Each built-in takes its `binary`, `label`, `args` and `icon` (an image URL; without one the page draws a badge of the CLI's initials in its color) from `executor.clis.<code>` when set. Any other key adds a plain CLI, so other tools need no code:

```yaml
erupt:
  ai-dev:
    executor:
      clis:
        # args: that CLI's own non-interactive flags
        zcode: {binary: zcode, label: Z Code, args: ["-p"]}
```

A CLI without a closing result event is judged by its exit code, its last text the answer. For a CLI that needs real parsing, implement `CodingAgentRunner` as a Spring bean.

### Running agents safely

An instance runs agents as soon as a coding CLI is installed on it, and approving a requirement needs one there. An agent edits files and runs commands with the server's account, so:

- Install a CLI on one instance only, ideally a container or VM dedicated to it.
- The agent process gets a clean environment (`PATH`, `HOME`, `LANG`, …) plus `erupt.ai-dev.executor.env` only. Server secrets such as database passwords never reach it.
- Git credentials never reach it either: erupt does every fetch and push itself.
- A credential goes only to the hosts listed under `erupt.ai-dev.credentials.<key>.hosts` (`github.com`, `*.example.com`, or `*` for any). None listed means it is never used, so a repository URL typed into a project cannot carry it to another server.
- Claude Code also loads the account's own `~/.claude` settings and hooks. Point `CLAUDE_CONFIG_DIR` at a dedicated directory to keep them apart.
- `executor.clis.<code>.args` grants the tools a headless run may use. For Claude Code, `Bash` means any command on that host.

```yaml
erupt:
  ai-dev:
    executor:
      max-concurrent: 2
      run-timeout-minutes: 60
      env:
        ANTHROPIC_API_KEY: ${ANTHROPIC_API_KEY}
        CLAUDE_CONFIG_DIR: /data/erupt-ai-dev/claude
        GIT_AUTHOR_NAME: Claude Code
        GIT_AUTHOR_EMAIL: agent@erupt.local
        GIT_COMMITTER_NAME: Claude Code
        GIT_COMMITTER_EMAIL: agent@erupt.local
      clis:
        claude:
          binary: claude
          args: ["--permission-mode", "acceptEdits", "--allowedTools", "Read,Edit,MultiEdit,Write,Glob,Grep,Bash"]
```

## Models

| Menu | Model | |
|---|---|---|
| Dev Project | `DevProject` | Code (unique per tenant, also the directory name), name, `AGENTS.md`, Active Workspaces (requirement workspaces still on the server) with *Clean Up Leftover Workspaces* for delivered, cancelled and deleted requirements, Self Review (off by default: the creator of a requirement cannot approve or reject it), and two tabs: repositories and members |
| *(hidden)* | `DevRequirement` | Lives in the project picked in the page header; numbered `REQ-n` per project; stage, version and progress are server-controlled. Its menu stays, hidden, because the pool writes through its add API and row operations |
| — | `DevAgentSession` / `DevAgentEvent` | Plain JPA entities, shown only by the pool: one row per agent run (status, CLI session id, cost, tokens) and its full log |
| — | `DevRequirementDelivery` | Plain JPA entity, shown only by the pool: one row per requirement × repository push |

## Project scope

`DevProjectScope` is an `@EruptScope` form whose only field is a `CHOICE` of the user's own projects. `DevRequirement` declares it, so the page header picks a project once and every list, detail and write follows it. The options are re-checked on every request, so a hand-edited `erupt-scope` header cannot reach a project the user is not a member of.

## Access

- Members only: a non-member never lists, opens or picks a project. Super admins see all.
- The creator becomes `OWNER`. Owners and maintainers change settings, repositories and members; a project keeps at least one of them.
- Every entity carries `@TenantId`, and every path on disk is prefixed with the tenant.

## Repositories on disk

```
{root}/repos/{tenant}/{project}/{alias}.git                bare clone, synced in the background after each save
{root}/worktrees/{tenant}/{project}/{req-n}/{alias}        worktree; {req-n} is the agent's working directory
```

Every segment is validated (project code, alias, requirement code, tenant), and every path is checked against the root.

## Configuration

```yaml
erupt:
  ai-dev:
    root: /data/erupt-ai-dev          # blank = ~/.erupt/ai-dev
    credentials:                      # a repository names its entry by key
      github:
        token: ${GITHUB_TOKEN}
      internal:
        username: deploy
        password: ${GIT_INTERNAL_PASSWORD}
      gitlab-ssh:
        ssh-key: /etc/erupt/keys/gitlab
```

Secrets stay in the configuration (inject them through environment variables or a secret manager), never in the database.

## Workspaces

A requirement's workspace (one worktree per repository on the branch `req/req-n`) lives as long as it can still run: delivery removes it, and so does cancelling. The requirement page shows it per repository (present or not, commits ahead of the base, uncommitted files; the server path for super admins), and *Clean Up Workspace* throws it away (worktrees, uncommitted changes and the local branch) for a draft, a requirement in review, a failed or a cancelled one, so the next run starts from the latest base. Nothing pushed is touched.
