#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <new-version>" >&2
    exit 1
fi

new_version="$1"
repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${repo_root}"

mvn -Dskip.web=true -DnewVersion="${new_version}" versions:set -DgenerateBackupPoms=false
npm version "${new_version}" --no-git-tag-version --allow-same-version

echo "Plugin version updated to ${new_version}."
