---
name: release
description: Standard operating procedure for shipping a new erupt version — bump, Maven Central, Docker Hub, merge develop→master + tags on erupt/erupt-web/erupt-pro, GitHub + gitee sync. Use when the user says /release, "发新版", "发布 x.y.z", "release erupt", or asks to publish to Maven Central / Docker Hub / tag a release.
---

# erupt release SOP

Usage: `/release <version>` (e.g. `/release 2.1.2`). Without a version, read the current one from
`pom.xml`, propose the next patch, and ask before continuing.

All mechanical work lives in `scripts/release.sh <phase> [version]`. Run the phases **in the order
below, one at a time**, read each phase's output before starting the next, and stop on the first
failure. Every phase is idempotent, so re-running after a fix is safe. Never skip `preflight`.

Repos involved (siblings under `~/git`, override with `CODE_DIR`):

| Repo | Role | Branches | Pushed to |
|------|------|----------|-----------|
| `erupt` | backend, publishes `xyz.erupt:*` | develop → master | GitHub + gitee (both push URLs on `origin`) |
| `erupt-web` | Angular frontend, bundled into `erupt/erupt-web` | develop → master | GitHub + gitee |
| `erupt-pro` | commercial modules | master only | GitHub + gitee |
| `erupt-start` | landing page, shows `ERUPT_VER` | main | GitHub |
| `erupt-cube` / `erupt-flow` / `erupt-tenant` / `erupt-demo` | satellites, consume erupt | own trains | bumped here, released separately |

## Phases

1. **`preflight <version>`** — tools, Central credentials, GPG key, Docker login, every tag repo
   clean and on develop, tag unused on both remotes. Read-only. Fix anything it reports before
   continuing; do not work around a red check.

2. **`frontend`** — pulls `~/git/erupt-web`, runs the production build into
   `erupt/erupt-web/src/main/resources/public`, commits `update erupt-web` if the bundle changed.
   Skip only if the user says the frontend is already current in this release.

3. **`bump <version>`** — runs `scripts/bump-erupt.sh` (main repo via `versions:set`, satellite
   poms by text edit) and commits `upgrade to <version>` in erupt. Satellites are left **bumped but
   uncommitted**; tell the user they need their own commit/tag/push and list them.

4. **`maven`** — `mvn clean deploy -P release -Dcentral.autoPublish=true -Dcentral.waitUntil=validated`.
   Runs the test suite; `SKIP_TESTS=1` only if the user asks. Takes several minutes; run it in the
   background and wait for the notification. On a validation failure, the Central Portal shows the
   reason at https://central.sonatype.com/publishing/deployments — quote it to the user. Never
   re-run bump between maven and docker: both must ship the same version.

5. **`docker`** — packages `erupt-deploy/erupt-docker`, builds `erupts/erupt:<version>` and
   `:latest`, pushes both. Starts Docker Desktop if it is down. If it reports no Docker Hub login,
   ask the user to run `! docker login` and re-run the phase.

6. **`publish <version>`** — for each repo in `TAG_REPOS` (default `erupt erupt-web erupt-pro`):
   pull develop, merge `--no-ff` into master, tag `<version>`, push master + tag to `origin`, which
   fans out to GitHub and gitee. Repos without a develop branch are tagged at master HEAD.
   **Confirm with the user before this phase** — it is the point of no return (tags on public
   remotes). Before confirming, check that erupt-pro actually has commits since its last tag
   (`git -C ~/git/erupt-pro log --oneline $(git -C ~/git/erupt-pro describe --tags --abbrev=0)..HEAD`);
   if it has none, run with `TAG_REPOS="erupt erupt-web"` and say so.

7. **`start <version>`** — rewrites `const ERUPT_VER` in `erupt-start/index.html`, commits
   `Bump ERUPT_VER to <version>`, pushes.

8. **`verify <version>`** — tags on every push remote, `repo1.maven.org` serving the new pom,
   Docker Hub tag present. Central sync takes 10–30 minutes; a Central WARN right after publish is
   expected, tell the user to re-run `verify` later rather than treating it as a failure.

## Rules

- Do the phases strictly in order. Publishing tags before Central has the artifacts means users
  see a version they cannot download.
- One version per run. If a phase fails after `bump`, fix and continue with the **same** version;
  never bump again to "get a clean number".
- Do not `--force` push, delete, or move a tag. If a tag went out wrong, tell the user and let them
  decide on a follow-up patch release.
- Chinese is fine in the conversation; commit messages and tags follow the existing conventions
  (`update erupt-web`, `upgrade to x.y.z`, tag `x.y.z` without a `v` prefix).

## Final report

Summarize: version, commit hashes for the frontend and bump commits, Central deployment status,
Docker tags pushed, per-repo tag + remote table from `verify`, and the list of satellite repos
whose poms were bumped but not yet released.
