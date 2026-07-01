#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
compose_file="${repo_root}/src/integration-test/graylog/docker-compose.yml"
project_name="${COMPOSE_PROJECT_NAME:-graylog-obfuscation-plugin-it}"
graylog_port="${GRAYLOG_HTTP_PORT:-19000}"
graylog_url="http://127.0.0.1:${graylog_port}"
auth="${GRAYLOG_ROOT_USER:-admin}:${GRAYLOG_ROOT_PASSWORD:-admin}"
root_password="${GRAYLOG_ROOT_PASSWORD:-admin}"
maven_repo="${MAVEN_REPO_LOCAL:-${repo_root}/.m2/repository}"
build_target="${SMOKE_BUILD_TARGET:-package}"
plugin_unique_id="com.netcracker.graylog2.plugin.ObfuscationPlugin"
curl_local=(curl --noproxy '*')

cd "${repo_root}"

if [[ -z "${JAVA_HOME:-}" && -x /usr/lib/jvm/java-17-openjdk/bin/java ]]; then
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
    export PATH="${JAVA_HOME}/bin:${PATH}"
fi

MAVEN_REPO_LOCAL="${maven_repo}" make "${build_target}"

plugin_jar="$(find "${repo_root}/target" -maxdepth 1 -name 'graylog-obfuscation-plugin-*.jar' ! -name '*original*' | head -n 1)"
if [[ -z "${plugin_jar}" ]]; then
    echo "Plugin JAR was not created in target/." >&2
    exit 1
fi

plugin_module="plugin.com.netcracker.graylog2.plugin.ObfuscationPlugin.module.json"
plugin_bundle="$(jar tf "${plugin_jar}" \
    | grep -E '^plugin\.com\.netcracker\.graylog2\.plugin\.ObfuscationPlugin\.[^.]+\.js$' \
    | head -n 1)"

if ! jar tf "${plugin_jar}" | grep -q "^${plugin_module}$"; then
    echo "Plugin JAR does not contain the frontend module manifest ${plugin_module}." >&2
    exit 1
fi

if [[ -z "${plugin_bundle}" ]]; then
    echo "Plugin JAR does not contain the frontend JavaScript bundle." >&2
    exit 1
fi

cleanup() {
    docker compose -p "${project_name}" -f "${compose_file}" down -v --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

export PLUGIN_JAR="${plugin_jar}"
export GRAYLOG_HTTP_PORT="${graylog_port}"
export GRAYLOG_ROOT_PASSWORD_SHA2="${GRAYLOG_ROOT_PASSWORD_SHA2:-$(printf '%s' "${root_password}" | sha256sum | awk '{print $1}')}"

docker compose -p "${project_name}" -f "${compose_file}" up -d

for attempt in $(seq 1 120); do
    if "${curl_local[@]}" -fsS "${graylog_url}/api/system/lbstatus" >/dev/null 2>&1; then
        break
    fi

    if [[ "${attempt}" == "120" ]]; then
        docker compose -p "${project_name}" -f "${compose_file}" logs graylog
        echo "Graylog did not become ready at ${graylog_url}." >&2
        exit 1
    fi

    sleep 5
done

plugins_json="$("${curl_local[@]}" -fsS -u "${auth}" -H 'X-Requested-By: graylog-smoke-test' \
    "${graylog_url}/api/system/plugins")"

if ! grep -q 'graylog-obfuscation-plugin' <<<"${plugins_json}"; then
    echo "Graylog started, but the obfuscation plugin was not listed by /api/system/plugins." >&2
    echo "${plugins_json}" >&2
    exit 1
fi

index_html="$("${curl_local[@]}" -fsS "${graylog_url}/")"
plugin_asset_path="/assets/plugin/${plugin_unique_id}/${plugin_bundle}"

if ! grep -q "${plugin_asset_path}" <<<"${index_html}"; then
    echo "Graylog index page does not reference the obfuscation plugin UI bundle ${plugin_asset_path}." >&2
    exit 1
fi

plugin_bundle_js="$("${curl_local[@]}" -fsS "${graylog_url}${plugin_asset_path}")"
if ! grep -q 'com.netcracker.graylog2.plugin' <<<"${plugin_bundle_js}"; then
    echo "Graylog served the plugin UI bundle, but it does not look like the obfuscation plugin bundle." >&2
    exit 1
fi

configuration='{
  "is-obfuscation-enabled": true,
  "text-replacer": "Static Star Replacer",
  "stream-titles": ["Audit logs"],
  "field-names": ["message"],
  "sensitive-regular-expressions": [
    {
      "id": 1,
      "name": "Social Security Number",
      "pattern": "(?<![\\d\\p{IsAlphabetic}]-?)(?>(?!000)(?:[0-6][0-4]\\d)-(?!00)\\d{2}-(?!0000)\\d{4})(?!-?[\\d\\p{IsAlphabetic}])",
      "importance": 1
    }
  ],
  "white-regular-expressions": []
}'

"${curl_local[@]}" -fsS -u "${auth}" -H 'X-Requested-By: graylog-smoke-test' -H 'Content-Type: application/json' \
    -X POST --data "${configuration}" \
    "${graylog_url}/api/plugins/com.netcracker.graylog2.plugin/obfuscation/configuration" >/dev/null

response="$("${curl_local[@]}" -fsS -u "${auth}" -H 'X-Requested-By: graylog-smoke-test' -H 'Content-Type: text/plain' \
    -X POST --data '123-12-1234' \
    "${graylog_url}/api/plugins/com.netcracker.graylog2.plugin/obfuscation")"

if ! grep -q '"obfuscated_text":"\*\*\*\*\*\*\*\*"' <<<"${response}"; then
    echo "Plugin REST endpoint returned an unexpected response:" >&2
    echo "${response}" >&2
    exit 1
fi

echo "Graylog loaded the plugin, served the plugin UI bundle, and the obfuscation REST endpoint works."
