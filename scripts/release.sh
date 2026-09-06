#!/usr/bin/env bash
#
# release.sh — Mechanical steps of the erupt release SOP, one phase per call.
#
# The /release skill (.claude/skills/release/SKILL.md) drives these phases in
# order and handles the judgement calls between them (version choice, reading
# build output, confirming before irreversible steps). Every phase is safe to
# re-run: it checks the current state first and skips work already done.
#
# Usage:
#   scripts/release.sh preflight <version>   # tools, credentials, clean trees, version unused
#   scripts/release.sh frontend              # erupt-web/build.sh (yarn build -> public), commit
#   scripts/release.sh bump <version>        # bump poms (main + satellites), commit "upgrade to <version>"
#   scripts/release.sh maven                 # scripts/deploy.sh with AUTO_PUBLISH=1 (Maven Central)
#   scripts/release.sh docker                # erupt-deploy/erupt-docker/deploy.sh (Docker Hub)
#   scripts/release.sh publish <version>     # merge develop -> master, tag, push (GitHub + gitee)
#   scripts/release.sh start <version>       # bump ERUPT_VER in erupt-start landing page, push
#   scripts/release.sh verify <version>      # tags on both remotes, Maven Central, Docker Hub
#
# Env:
#   CODE_DIR       Parent directory of the repos          (default: $HOME/git)
#   TAG_REPOS      Repos to merge/tag/push in `publish`   (default: "erupt erupt-web erupt-pro")
#   SKIP_TESTS=1   Skip tests during the Maven deploy
#   DOCKER_IMAGE   Image name                             (default: erupts/erupt)

set -euo pipefail

CODE_DIR="${CODE_DIR:-$HOME/git}"
ERUPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TAG_REPOS="${TAG_REPOS:-erupt erupt-web erupt-pro}"
DOCKER_IMAGE="${DOCKER_IMAGE:-erupts/erupt}"
DOCKER_DIR="$ERUPT_DIR/erupt-deploy/erupt-docker"
WEB_SRC_DIR="$CODE_DIR/erupt-web"
START_DIR="$CODE_DIR/erupt-start"

# ---------- output helpers ----------
say()  { printf '\033[1;34m==>\033[0m %s\n' "$*"; }
ok()   { printf '\033[1;32m[OK]\033[0m   %s\n' "$*"; }
warn() { printf '\033[1;33m[WARN]\033[0m %s\n' "$*"; }
skip() { printf '\033[1;90m[SKIP]\033[0m %s\n' "$*"; }
die()  { printf '\033[1;31m[FAIL]\033[0m %s\n' "$*" >&2; exit 1; }

usage() { sed -n '2,/^$/p' "$0" | sed 's/^# \{0,1\}//'; exit 1; }

# ---------- git helpers ----------
project_version() {
    # The main pom's own <version>, ignoring the <parent> block
    python3 -c '
import re, sys
text = open(sys.argv[1]).read()
clean = re.sub(r"<parent\b.*?</parent>", "", text, flags=re.DOTALL)
m = re.search(r"<version>\s*([^<\s]+)\s*</version>", clean)
print(m.group(1) if m else "")' "$ERUPT_DIR/pom.xml"
}

require_version() {
    [ -n "${1:-}" ] || die "version argument required, e.g. 2.1.2"
    [[ "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || die "version must look like x.y.z, got: $1"
}

git_clean() { [ -z "$(git -C "$1" status --porcelain --untracked-files=no)" ]; }

git_has_branch() { git -C "$1" show-ref --verify --quiet "refs/heads/$2"; }

git_tag_exists_remote() {
    # $1 repo dir, $2 tag, $3 remote url
    git -C "$1" ls-remote --exit-code --tags "$3" "refs/tags/$2" >/dev/null 2>&1
}

push_urls() { git -C "$1" remote get-url --push --all origin; }

# ---------- phases ----------
phase_preflight() {
    require_version "$1"; local ver="$1"
    say "Preflight for release $ver"

    for tool in git mvn java gpg docker yarn python3; do
        command -v "$tool" >/dev/null || die "missing tool: $tool"
    done
    ok "tools present: git mvn java gpg docker yarn python3"

    local jv; jv=$(java -version 2>&1 | head -1)
    [[ "$jv" == *'"17'* ]] || warn "java is not 17: $jv"

    grep -q "<id>central</id>" "$HOME/.m2/settings.xml" 2>/dev/null \
        || die "~/.m2/settings.xml has no <server><id>central</id> credentials for Maven Central"
    ok "Maven Central credentials (server id 'central') present"

    gpg --list-secret-keys 2>/dev/null | grep -q sec || die "no GPG secret key for artifact signing"
    ok "GPG signing key present"

    if docker info >/dev/null 2>&1; then
        ok "docker daemon running"
    else
        warn "docker daemon not running; the docker phase will try 'open -a Docker'"
    fi
    python3 -c 'import json,sys;d=json.load(open(sys.argv[1]));sys.exit(0 if "https://index.docker.io/v1/" in d.get("auths",{}) else 1)' \
        "$HOME/.docker/config.json" 2>/dev/null && ok "Docker Hub login present" \
        || warn "not logged in to Docker Hub; run 'docker login' before the docker phase"

    for repo in $TAG_REPOS; do
        local dir="$CODE_DIR/$repo"
        [ -d "$dir/.git" ] || die "repo missing: $dir"
        git_clean "$dir" || die "$repo has uncommitted changes"
        local br; br=$(git -C "$dir" branch --show-current)
        if git_has_branch "$dir" develop && [ "$br" != "develop" ]; then
            die "$repo is on '$br', expected develop"
        fi
        for url in $(push_urls "$dir"); do
            git_tag_exists_remote "$dir" "$ver" "$url" && die "$repo: tag $ver already exists on $url"
        done
        ok "$repo clean on $br, tag $ver unused on $(push_urls "$dir" | wc -l | tr -d ' ') push remote(s)"
    done

    [ -d "$WEB_SRC_DIR/.git" ] || die "frontend repo missing: $WEB_SRC_DIR"
    local cur; cur=$(project_version)
    echo
    echo "current version : $cur"
    echo "target version  : $ver"
    echo "tag repos       : $TAG_REPOS"
}

phase_frontend() {
    say "Building erupt-web frontend via erupt-web/build.sh"
    # build.sh uses paths relative to erupt/erupt-web: pulls ../../erupt-web, yarn build, git add public
    (cd "$ERUPT_DIR/erupt-web" && bash build.sh)
    cd "$ERUPT_DIR"
    if git diff --cached --quiet; then
        skip "frontend bundle unchanged, nothing to commit"
    else
        git commit -q -m "update erupt-web"
        ok "committed frontend bundle: $(git log -1 --format=%h)"
    fi
}

phase_bump() {
    require_version "$1"; local ver="$1"
    local cur; cur=$(project_version)
    if [ "$cur" = "$ver" ]; then
        skip "erupt already at $ver"
    else
        say "Bumping $cur -> $ver (main repo + satellites via bump-erupt.sh)"
        CODE_DIR="$CODE_DIR" "$ERUPT_DIR/scripts/bump-erupt.sh" "$ver"
    fi
    cd "$ERUPT_DIR"
    if git_clean "$ERUPT_DIR"; then
        skip "no version changes to commit in erupt"
    else
        git add -A -- '*.xml'
        git commit -q -m "upgrade to $ver"
        ok "committed: $(git log -1 --format='%h %s')"
    fi
    for repo in erupt-cube erupt-flow erupt-tenant erupt-demo; do
        local dir="$CODE_DIR/$repo"
        [ -d "$dir/.git" ] || continue
        git_clean "$dir" || warn "$repo: pom bumped but NOT committed — separate release train, review and push by hand"
    done
}

phase_maven() {
    say "Deploying to Maven Central via scripts/deploy.sh"
    AUTO_PUBLISH=1 SKIP_TESTS="${SKIP_TESTS:-0}" "$ERUPT_DIR/scripts/deploy.sh"
    ok "deployment uploaded and validated; Central publishes it automatically (autoPublish=true)"
}

phase_docker() {
    local ver; ver=$(project_version)
    say "Building and pushing $DOCKER_IMAGE:$ver via erupt-deploy/erupt-docker/deploy.sh"
    if ! docker info >/dev/null 2>&1; then
        warn "starting Docker Desktop"
        open -a Docker
        for _ in $(seq 1 60); do docker info >/dev/null 2>&1 && break; sleep 2; done
        docker info >/dev/null 2>&1 || die "docker daemon did not come up"
    fi
    python3 -c 'import json,sys;d=json.load(open(sys.argv[1]));sys.exit(0 if "https://index.docker.io/v1/" in d.get("auths",{}) else 1)' \
        "$HOME/.docker/config.json" 2>/dev/null || die "not logged in to Docker Hub; run: docker login"
    DOCKER_IMAGE="$DOCKER_IMAGE" "$DOCKER_DIR/deploy.sh"
}

phase_publish() {
    require_version "$1"; local ver="$1"
    for repo in $TAG_REPOS; do
        local dir="$CODE_DIR/$repo"
        [ -d "$dir/.git" ] || { warn "$repo missing, skipped"; continue; }
        say "$repo: merge develop -> master, tag $ver, push"
        git_clean "$dir" || die "$repo has uncommitted changes"
        git -C "$dir" fetch -q origin

        if git_has_branch "$dir" develop; then
            git -C "$dir" checkout -q develop
            git -C "$dir" pull -q --ff-only origin develop
            git -C "$dir" push -q origin develop
            git -C "$dir" checkout -q master
            git -C "$dir" pull -q --ff-only origin master
            if git -C "$dir" merge-base --is-ancestor develop master; then
                skip "$repo: master already contains develop"
            else
                git -C "$dir" merge --no-ff -q -m "Merge develop into master for release $ver" develop
                ok "$repo: merged develop into master"
            fi
        else
            git -C "$dir" checkout -q master
            git -C "$dir" pull -q --ff-only origin master
            warn "$repo: no develop branch, tagging master HEAD as is"
        fi

        if git -C "$dir" rev-parse -q --verify "refs/tags/$ver" >/dev/null; then
            skip "$repo: tag $ver already exists locally"
        else
            git -C "$dir" tag "$ver"
            ok "$repo: tagged $ver at $(git -C "$dir" rev-parse --short HEAD)"
        fi

        # origin carries both GitHub and gitee push URLs, so one push syncs both
        git -C "$dir" push origin master "refs/tags/$ver"
        for url in $(push_urls "$dir"); do
            git_tag_exists_remote "$dir" "$ver" "$url" && ok "$repo: $ver on $url" || warn "$repo: $ver NOT on $url"
        done
        git_has_branch "$dir" develop && git -C "$dir" checkout -q develop
    done
}

phase_start() {
    require_version "$1"; local ver="$1"
    local f="$START_DIR/index.html"
    [ -f "$f" ] || { warn "erupt-start not found at $START_DIR, skipped"; return; }
    say "erupt-start: ERUPT_VER -> $ver"
    if grep -q "const ERUPT_VER = '$ver'" "$f"; then
        skip "erupt-start already at $ver"
        return
    fi
    git_clean "$START_DIR" || die "erupt-start has uncommitted changes"
    git -C "$START_DIR" pull -q --ff-only
    sed -i '' "s/const ERUPT_VER = '[^']*'/const ERUPT_VER = '$ver'/" "$f"
    git -C "$START_DIR" commit -q -am "Bump ERUPT_VER to $ver"
    git -C "$START_DIR" push -q origin HEAD
    ok "erupt-start bumped and pushed"
}

phase_verify() {
    require_version "$1"; local ver="$1"
    say "Verifying release $ver"
    for repo in $TAG_REPOS; do
        local dir="$CODE_DIR/$repo"
        [ -d "$dir/.git" ] || continue
        for url in $(push_urls "$dir"); do
            git_tag_exists_remote "$dir" "$ver" "$url" && ok "$repo tag $ver: $url" || warn "$repo tag $ver missing: $url"
        done
    done
    local central="https://repo1.maven.org/maven2/xyz/erupt/erupt/$ver/erupt-$ver.pom"
    if curl -sfI "$central" >/dev/null; then ok "Maven Central: $central"
    else warn "Maven Central not yet serving $ver (sync takes ~10-30 min): $central"; fi
    local hub="https://hub.docker.com/v2/repositories/$DOCKER_IMAGE/tags/$ver"
    if curl -sf "$hub" >/dev/null; then ok "Docker Hub: $DOCKER_IMAGE:$ver"
    else warn "Docker Hub tag not found: $DOCKER_IMAGE:$ver"; fi
}

# ---------- dispatch ----------
[ $# -ge 1 ] || usage
phase="$1"; shift
case "$phase" in
    preflight) phase_preflight "${1:-}" ;;
    frontend)  phase_frontend ;;
    bump)      phase_bump "${1:-}" ;;
    maven)     phase_maven ;;
    docker)    phase_docker ;;
    publish)   phase_publish "${1:-}" ;;
    start)     phase_start "${1:-}" ;;
    verify)    phase_verify "${1:-}" ;;
    -h|--help) usage ;;
    *) die "unknown phase: $phase" ;;
esac
