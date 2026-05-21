#!/usr/bin/env bash
#
# bump-erupt.sh — Bump <erupt.version> across all satellite repos in one shot.
#
# Each satellite repo must declare:
#     <properties>
#         <erupt.version>X.Y.Z</erupt.version>
#     </properties>
# and reference it via ${erupt.version} on erupt dependencies.
#
# Usage:
#   ./bump-erupt.sh 1.14.4                # bump everything to 1.14.4
#   ./bump-erupt.sh --dry-run 1.14.4      # preview without writing
#   CODE_DIR=/path/to/code ./bump-erupt.sh 1.14.4
#
# Env overrides:
#   CODE_DIR         Parent directory of repos (default: $HOME/code)
#   VERSIONS_PLUGIN  Maven versions plugin coordinate (default below)

set -euo pipefail

# ---------- config ----------
CODE_DIR="${CODE_DIR:-$HOME/code}"
VERSIONS_PLUGIN="${VERSIONS_PLUGIN:-org.codehaus.mojo:versions-maven-plugin:2.16.2}"

# Satellite repos (relative to CODE_DIR). Add or remove freely.
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
  CODE_DIR         Parent of repos (default: \$HOME/code)
  VERSIONS_PLUGIN  Maven plugin coordinate
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

# ---------- run ----------
echo "Code dir       : $CODE_DIR"
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
        echo "[SKIP] $repo  (no pom.xml — not a maven project)"
        skipped+=("$repo")
        continue
    fi
    if ! grep -q '<erupt\.version>' "$dir/pom.xml"; then
        echo "[SKIP] $repo  (no <erupt.version> property in pom.xml)"
        skipped+=("$repo")
        continue
    fi

    current=$(grep -oE '<erupt\.version>[^<]+' "$dir/pom.xml" \
              | head -1 | sed 's|<erupt.version>||')

    if [ "$current" = "$NEW_VERSION" ]; then
        echo "[OK]   $repo  (already $NEW_VERSION)"
        ok+=("$repo")
        continue
    fi

    if [ "$DRY_RUN" -eq 1 ]; then
        echo "[PLAN] $repo  $current -> $NEW_VERSION"
        ok+=("$repo")
        continue
    fi

    echo "[BUMP] $repo  $current -> $NEW_VERSION"
    if ( cd "$dir" && mvn -q "${VERSIONS_PLUGIN}:set-property" \
            -Dproperty=erupt.version \
            -DnewVersion="$NEW_VERSION" \
            -DgenerateBackupPoms=false ); then
        ok+=("$repo")
    else
        echo "[FAIL] $repo  (mvn exited non-zero)" >&2
        failed+=("$repo")
    fi
done

echo
echo "=== Summary ==="
printf "OK      (%d): %s\n" "${#ok[@]}"      "${ok[*]:-none}"
printf "Skipped (%d): %s\n" "${#skipped[@]}" "${skipped[*]:-none}"
printf "Failed  (%d): %s\n" "${#failed[@]}"  "${failed[*]:-none}"

[ "${#failed[@]}" -gt 0 ] && exit 1 || exit 0