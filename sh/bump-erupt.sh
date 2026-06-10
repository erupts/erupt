#!/usr/bin/env bash
#
# bump-erupt.sh — Bump erupt's version across the main repo and all satellites.
#
# Strategy:
#   * Main repo (publishes erupt artifacts) -> `versions:set` updates its own
#     project version and cascades into all submodules.
#   * Satellite repos (consume erupt)       -> `versions:use-dep-version`
#     updates every <dependency>/<parent> matching the erupt groupId, no
#     matter whether the version is inline, a property reference, or in the
#     parent declaration.
#
# Usage:
#   ./bump-erupt.sh 1.14.4
#   ./bump-erupt.sh --dry-run 1.14.4
#   CODE_DIR=/path/to/code ./bump-erupt.sh 1.14.4
#
# Env overrides:
#   CODE_DIR         Parent directory of repos     (default: $HOME/code)
#   MAIN_REPO        Which entry in REPOS is main  (default: erupt)
#   ERUPT_GROUP      Maven groupId to bump         (default: xyz.erupt)
#   VERSIONS_PLUGIN  Plugin coordinate             (default below)

set -euo pipefail

# ---------- config ----------
CODE_DIR="${CODE_DIR:-$HOME/git}"
MAIN_REPO="${MAIN_REPO:-erupt}"
ERUPT_GROUP="${ERUPT_GROUP:-xyz.erupt}"
VERSIONS_PLUGIN="${VERSIONS_PLUGIN:-org.codehaus.mojo:versions-maven-plugin:2.16.2}"

# Repos to bump (relative to CODE_DIR). Main repo first by convention.
REPOS=(
    erupt
    erupt-bi
    erupt-cube
    erupt-flow
    erupt-tenant
    erupt-demo
)

# ---------- args ----------
DRY_RUN=0
NEW_VERSION=""

usage() {
    cat <<EOF
Usage: $(basename "$0") [--dry-run] <new-version>

Options:
  --dry-run   Show what would change, don't write.
  -h, --help  Show this help.

Env:
  CODE_DIR         Parent of repos      (default: \$HOME/code)
  MAIN_REPO        Self-publishing repo (default: erupt)
  ERUPT_GROUP      Maven groupId        (default: xyz.erupt)
  VERSIONS_PLUGIN  Plugin coordinate    (default: $VERSIONS_PLUGIN)
EOF
    exit 1
}

while [ $# -gt 0 ]; do
    case "$1" in
        --dry-run) DRY_RUN=1; shift ;;
        -h|--help) usage ;;
        -*)        echo "unknown flag: $1" >&2; usage ;;
        *)         NEW_VERSION="$1"; shift ;;
    esac
done

[ -z "$NEW_VERSION" ] && usage

# ---------- helpers ----------
# The project's own version (top-level <version>, ignoring <parent>).
project_version() {
    python3 -c '
import re, sys
with open(sys.argv[1]) as f: text = f.read()
clean = re.sub(r"<parent\b.*?</parent>", "", text, flags=re.DOTALL)
m = re.search(r"<version>\s*([^<\s]+)\s*</version>", clean)
print(m.group(1) if m else "")
' "$1" 2>/dev/null
}

# All erupt versions referenced in <dependency>/<parent> blocks (uniq).
current_erupt_versions() {
    python3 -c '
import re, sys
with open(sys.argv[1]) as f: text = f.read()
grp = sys.argv[2]
for m in re.finditer(r"<(dependency|parent)\b[^>]*>(.*?)</\1>", text, re.DOTALL):
    block = m.group(2)
    g = re.search(r"<groupId>\s*([^<\s]+)\s*</groupId>", block)
    v = re.search(r"<version>\s*([^<\s]+)\s*</version>", block)
    if g and v and g.group(1) == grp:
        print(v.group(1))
' "$1" "$ERUPT_GROUP" 2>/dev/null | sort -u | tr '\n' ' '
}

# ---------- run ----------
echo "Code dir       : $CODE_DIR"
echo "Main repo      : $MAIN_REPO"
echo "Erupt groupId  : $ERUPT_GROUP"
echo "Target version : $NEW_VERSION"
[ "$DRY_RUN" -eq 1 ] && echo "Mode           : DRY RUN"
echo

ok=()
skipped=()
failed=()

for repo in "${REPOS[@]}"; do
    dir="$CODE_DIR/$repo"

    if [ ! -d "$dir" ]; then
        echo "[SKIP] $repo  (directory missing)"
        skipped+=("$repo")
        continue
    fi
    if [ ! -f "$dir/pom.xml" ]; then
        echo "[SKIP] $repo  (no pom.xml)"
        skipped+=("$repo")
        continue
    fi

    # ----- branch: main repo (set project version) -----
    if [ "$repo" = "$MAIN_REPO" ]; then
        current=$(project_version "$dir/pom.xml")
        [ -z "$current" ] && current="?"

        if [ "$current" = "$NEW_VERSION" ]; then
            echo "[OK]   $repo  (already at $NEW_VERSION)"
            ok+=("$repo")
            continue
        fi

        if [ "$DRY_RUN" -eq 1 ]; then
            echo "[PLAN-SET] $repo  project version $current -> $NEW_VERSION"
            ok+=("$repo")
            continue
        fi

        echo "[BUMP-SET] $repo  project version $current -> $NEW_VERSION"
        if ( cd "$dir" && mvn -q "${VERSIONS_PLUGIN}:set" \
                -DnewVersion="$NEW_VERSION" \
                -DprocessAllModules=true \
                -DgenerateBackupPoms=false ); then
            ok+=("$repo")
        else
            echo "[FAIL] $repo  (mvn versions:set exited non-zero)" >&2
            failed+=("$repo")
        fi
        continue
    fi

    # ----- branch: satellite repo (bump dep refs) -----
    if ! grep -q "<groupId>$ERUPT_GROUP" "$dir/pom.xml"; then
        echo "[SKIP] $repo  (no $ERUPT_GROUP dependency in pom)"
        skipped+=("$repo")
        continue
    fi

    current=$(current_erupt_versions "$dir/pom.xml")
    current="${current%% }"
    [ -z "$current" ] && current="(no literal version found)"

    if [ "$DRY_RUN" -eq 1 ]; then
        echo "[PLAN-DEP] $repo  [$current] -> $NEW_VERSION"
        ok+=("$repo")
        continue
    fi

    echo "[BUMP-DEP] $repo  [$current] -> $NEW_VERSION"
    if ( cd "$dir" && mvn -q "${VERSIONS_PLUGIN}:use-dep-version" \
            -Dincludes="$ERUPT_GROUP:*" \
            -DdepVersion="$NEW_VERSION" \
            -DforceVersion=true \
            -DprocessParent=true \
            -DgenerateBackupPoms=false ); then
        ok+=("$repo")
    else
        echo "[FAIL] $repo  (mvn use-dep-version exited non-zero)" >&2
        failed+=("$repo")
    fi
done

echo
echo "=== Summary ==="
printf "OK      (%d): %s\n" "${#ok[@]}"      "${ok[*]:-none}"
printf "Skipped (%d): %s\n" "${#skipped[@]}" "${skipped[*]:-none}"
printf "Failed  (%d): %s\n" "${#failed[@]}"  "${failed[*]:-none}"

[ "${#failed[@]}" -gt 0 ] && exit 1 || exit 0