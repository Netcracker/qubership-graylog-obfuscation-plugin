#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
graylog_repo="${GRAYLOG_REPOSITORY:-https://github.com/Graylog2/graylog2-server.git}"
graylog_dir="${GRAYLOG_SOURCE_DIR:-${repo_root}/.graylog/graylog2-server}"
graylog_web_dir="${graylog_dir}/graylog2-web-interface"
prepare_deps="${GRAYLOG_WEB_PREPARE_DEPS:-true}"

if [[ -n "${GRAYLOG_VERSION:-}" ]]; then
    graylog_ref="${GRAYLOG_VERSION}"
else
    graylog_ref="$(sed -n 's:.*<graylog.version>\(.*\)</graylog.version>.*:\1:p' "${repo_root}/pom.xml" | head -n 1)"
fi

if [[ -z "${graylog_ref}" ]]; then
    echo "Cannot determine Graylog version. Set GRAYLOG_VERSION explicitly." >&2
    exit 1
fi

mkdir -p "$(dirname "${graylog_dir}")"

if [[ ! -d "${graylog_dir}/.git" ]]; then
    git clone --depth 1 --branch "${graylog_ref}" "${graylog_repo}" "${graylog_dir}"
else
    git -C "${graylog_dir}" fetch --depth 1 origin "${graylog_ref}"
    git -C "${graylog_dir}" checkout --force --detach FETCH_HEAD
    git -C "${graylog_dir}" reset --hard FETCH_HEAD
    git -C "${graylog_dir}" clean -fd
fi

if [[ ! -d "${graylog_web_dir}/packages/graylog-web-plugin" ]]; then
    echo "Graylog checkout does not contain graylog-web-plugin package." >&2
    exit 1
fi

if [[ "${prepare_deps}" == "true" ]]; then
    (
        cd "${graylog_web_dir}"
        PUPPETEER_SKIP_DOWNLOAD="${PUPPETEER_SKIP_DOWNLOAD:-true}" yarn install --frozen-lockfile

        yarn webpack --config webpack.vendor.ts
    )
fi

echo "Graylog web source is ready at ${graylog_web_dir}"
