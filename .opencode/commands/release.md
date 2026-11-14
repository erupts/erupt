---
description: Ship a new erupt version — merge develop→master on erupt/erupt-web and tag them with the changelog from erupt-docs, tag erupt-cube/erupt-flow/erupt-tenant
---

# erupt release SOP

Release version: **$ARGUMENTS** (call it `VER` below).

If no version was given above, read it from `<version>` in `erupt/pom.xml` and ask the user to confirm.

Publishing to Maven Central is done by the user, not by this command.

Repos are siblings under `~/Code`:

| Repo | Branches | What happens |
|------|----------|--------------|
| `erupt` | develop → master | merge, tag `VER` with the changelog |
| `erupt-web` | develop → master | merge, tag `VER` with the changelog |
| `erupt-cube` | main | tag `VER` |
| `erupt-flow` | main | tag `VER` |
| `erupt-tenant` | master | tag `VER` |

## Step 1 — merge and tag

### 1.1 Preflight (read-only, stop on any red check)

- `gh auth status` succeeds.
- Every repo above has a clean working tree, is on the branch in the table and is level with its remote
  after `git fetch`. Untracked `.flattened-pom.xml` files do not count.
- Tag `VER` exists neither locally nor on `origin` (`git ls-remote --tags origin VER`) in any repo.
- `erupt-cube`, `erupt-flow` and `erupt-tenant` already depend on erupt `VER` in their `pom.xml`, and
  each has commits since its previous tag (`git log --oneline $(git describe --tags --abbrev=0)..HEAD`).
- `~/Code/erupt-docs/en/guide/changelog.md` has a `## VER (` section. Read the local file as it is; do
  not pull or edit erupt-docs. If the section is missing, stop and ask the user.

### 1.2 Extract the changelog

Take the section from `## VER (` up to the next `## <digit>` heading, turn the docs-relative links into
absolute ones, and put the version line on top:

```bash
DOCS=~/Code/erupt-docs/en/guide/changelog.md
NOTES=<scratchpad>/release-notes-$VER.md
awk -v v="$VER" 'index($0, "## " v " (") == 1 {f = 1; print "erupt " substr($0, 4); next}
                 f && /^## [0-9]/ {exit} f' "$DOCS" \
  | sed -E 's#<Badge [^>]*text="([^"]*)"[^>]*/>#(\1)#; s#\]\(/(en|zh)/#](https://docs.erupt.xyz/\1/#g' > "$NOTES"
```

Show the user the first lines of `$NOTES` and its line count.

### 1.3 Merge develop → master (erupt, erupt-web)

For each of the two repos, through a pull request, never a direct push to master:

```bash
gh pr create --repo <owner/repo> --base master --head develop --title "erupt $VER" --body-file "$NOTES"
gh pr view <number> --json mergeable,mergeStateStatus
gh pr merge <number> --merge          # merge commit; never --delete-branch, develop stays
git checkout master && git pull && git checkout develop
```

If the PR is not mergeable (conflicts, failing required checks), stop and report — do not resolve
conflicts on master by hand without the user.

### 1.4 Tag

**Confirm with the user before pushing any tag** — tags on public remotes are the point of no return.
Show the commit each tag will point at.

- `erupt`, `erupt-web`: annotated tag on the master merge commit, message = the changelog.
  `--cleanup=verbatim` is required, otherwise git drops the `###` headings as comments.

  ```bash
  git tag -a "$VER" origin/master --cleanup=verbatim -F "$NOTES"
  git push origin "$VER"
  ```

- `erupt-cube`, `erupt-flow`, `erupt-tenant`: annotated tag on the current HEAD of their branch.

  ```bash
  git tag -a "$VER" -m "<repo> $VER"
  git push origin "$VER"
  ```

Tags are `VER` without a `v` prefix.

## Rules

- One version per run; never re-tag with another number to "get a clean one".
- Never `--force` push, delete or move a tag. If a tag went out wrong, tell the user and let them decide.
- Chinese is fine in the conversation; PR titles, commit and tag messages stay English.

## Final report

Version, the two merge PR links and merge commits, and a table of repo → tag → commit → pushed.
