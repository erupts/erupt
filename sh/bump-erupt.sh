#!/usr/bin/env bash
#
# bump-erupt.sh — Bump erupt's version across the main repo and all satellites.
#
# Strategy:
#   * Main repo (publishes erupt artifacts) -> `versions:set` updates its own
#     project version and cascades into all submodules.
#   * Satellite repos (consume erupt)       -> direct pom.xml text edits.
#     Maven-based bumping (versions:use-dep-version) cannot work here: it
#     needs to resolve the OLD erupt parent pom to read the project, which
#     typically no longer exists locally after the main repo was bumped.
#     We rewrite every <dependency>/<parent> matching the erupt groupId
#     (literal or ${...} property ref), following version property
#     indirections like <erupt.version>.
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
def is_erupt(gid):
    return gid == grp or (gid.startswith("${") and "erupt" in gid.lower())
for m in re.finditer(r"<(dependency|parent)\b[^>]*>(.*?)</\1>", text, re.DOTALL):
    block = m.group(2)
    g = re.search(r"<groupId>\s*([^<\s]+)\s*</groupId>", block)
    v = re.search(r"<version>\s*([^<\s]+)\s*</version>", block)
    if g and v and is_erupt(g.group(1)):
        print(v.group(1))
' "$1" "$ERUPT_GROUP" 2>/dev/null | sort -u | tr '\n' ' '
}

# Rewrite erupt versions in all pom.xml files under a repo (pure text edit,
# no Maven — works even when the old erupt parent is not resolvable).
# Prints the number of modified files.
bump_satellite_poms() {
    python3 - "$1" "$NEW_VERSION" "$ERUPT_GROUP" <<'PYEOF'
import re, sys, pathlib

root, new, grp = pathlib.Path(sys.argv[1]), sys.argv[2], sys.argv[3]

def is_erupt(gid):
    return gid == grp or (gid.startswith("${") and "erupt" in gid.lower())

changed = 0
for pom in sorted(root.rglob("pom.xml")):
    if "target" in pom.parts:
        continue
    text = pom.read_text(encoding="utf-8")
    props_to_bump = set()

    def bump_block(m):
        block = m.group(0)
        g = re.search(r"<groupId>\s*([^<\s]+)\s*</groupId>", block)
        if not g or not is_erupt(g.group(1)):
            return block

        def bump_version(vm):
            v = vm.group(1)
            if v.startswith("${"):
                prop = v[2:-1]
                # project.parent.version / project.version resolve on their own
                if not prop.startswith("project."):
                    props_to_bump.add(prop)
                return vm.group(0)
            return "<version>%s</version>" % new

        return re.sub(r"<version>\s*([^<\s]+)\s*</version>", bump_version, block)

    out = re.sub(r"<(dependency|parent)\b[^>]*>.*?</\1>", bump_block, text, flags=re.DOTALL)
    for prop in props_to_bump:
        out = re.sub(
            r"(<%s>)\s*[^<\s]+\s*(</%s>)" % (re.escape(prop), re.escape(prop)),
            r"\g<1>%s\g<2>" % new, out)
    if out != text:
        pom.write_text(out, encoding="utf-8")
        changed += 1

print(changed)
PYEOF
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
    if n=$(bump_satellite_poms "$dir"); then
        echo "           $n pom(s) updated"
        ok+=("$repo")
    else
        echo "[FAIL] $repo  (pom rewrite failed)" >&2
        failed+=("$repo")
    fi
done

echo
echo "=== Summary ==="
printf "OK      (%d): %s\n" "${#ok[@]}"      "${ok[*]:-none}"
printf "Skipped (%d): %s\n" "${#skipped[@]}" "${skipped[*]:-none}"
printf "Failed  (%d): %s\n" "${#failed[@]}"  "${failed[*]:-none}"

[ "${#failed[@]}" -gt 0 ] && exit 1 || exit 0