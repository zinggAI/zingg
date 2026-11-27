#!/usr/bin/env bash
# Build a pinned Zingg v0.7 common/client capsule without replacing ~/.m2 artifacts.
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)"
output_dir="${1:-$repo_root/dist/zingg07-capsule}"
commit="48cb157b4f35fcfa733e2e7f9699ea988e018738"
temp_parent="$(cd "${TMPDIR:-/tmp}" && pwd -P)"
source_dir="$(mktemp -d "$temp_parent/zingg07-capsule.XXXXXXXX")"

cleanup() {
  if [[ -d "$source_dir" ]]; then
    resolved="$(cd "$source_dir" && pwd -P)"
    if [[ "$(dirname "$resolved")" == "$temp_parent" && "$(basename "$resolved")" == zingg07-capsule.* ]]; then
      rm -rf -- "$resolved"
    fi
  fi
}
trap cleanup EXIT

git clone --filter=blob:none --no-checkout https://github.com/zinggai/zingg.git "$source_dir"
git -C "$source_dir" fetch --depth 1 origin "$commit"
git -C "$source_dir" sparse-checkout init --no-cone
git -C "$source_dir" sparse-checkout set --no-cone pom.xml common/pom.xml 'common/client/**' 'common/core/src/main/**' 'thirdParty/lib/**'
git -C "$source_dir" checkout --detach "$commit"
actual="$(git -C "$source_dir" rev-parse HEAD)"
[[ "$actual" == "$commit" ]] || { printf 'upstream commit mismatch: %s\n' "$actual" >&2; exit 1; }

"$repo_root/mvnw" -f "$source_dir/pom.xml" -pl common/client -am -DskipTests -Dzingg.version=0.7.0 package
source_jar="$source_dir/common/client/target/zingg-common-client-0.7.0.jar"
[[ -f "$source_jar" ]] || { printf 'upstream common/client JAR missing: %s\n' "$source_jar" >&2; exit 1; }

mkdir -p "$output_dir"
candidate="$output_dir/zingg-common-client-0.7.0.jar"
manifest="$output_dir/zingg07-capsule-manifest.json"
python3 "$repo_root/packaging/normalize-zingg07-jar.py" "$source_jar" "$candidate" "$manifest"
python3 "$repo_root/tools/verify-zframe-inventory.py" "$candidate"
