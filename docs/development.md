# Obfuscation Plugin Developer Guide

This guide describes how to build, test, and run the Graylog Obfuscation Plugin
locally.

The current development target is Graylog `5.2.x`. The plugin uses Graylog
`5.2.12`, Java 17, and the Graylog `5.2.12` frontend plugin toolchain.

## Prerequisites

Have these tools available in your development environment:

- JDK 17
- Maven 3.9 or newer
- Node.js 20, or another Node.js version compatible with the Graylog web
  interface checkout
- Yarn 1.22.x
- Git
- Docker with Docker Compose support, required for the integration smoke test
- `curl`, required by the integration smoke test

Use Java 17 when building the plugin:

```bash
java -version
mvn -version
```

## Build Targets

The repository provides a `Makefile` so local development and CI use the same
entry points.

```bash
make backend-test
make backend-package
make frontend-test
make frontend-build
make package
make smoke
```

`make backend-test` and `make backend-package` pass `-Dskip.web=true` to Maven.
That flag is only for backend work; it does not build or test the web UI.

`make package` performs a clean backend build, prepares Graylog web sources,
builds the plugin frontend, and packages a JAR containing the UI assets.

## Graylog Web Sources

The frontend build uses `graylog-web-plugin` directly from a local Graylog
source checkout. The checkout is managed by:

```bash
scripts/update-graylog-web.sh
```

By default the script:

1. Clones or updates Graylog from
   `https://github.com/Graylog2/graylog2-server.git`.
2. Checks out the Graylog version from `<graylog.version>` in `pom.xml`,
   currently `5.2.12`.
3. Runs `yarn install --frozen-lockfile` in `graylog2-web-interface`.
4. Builds the Graylog web vendor bundle required by plugin webpack builds.

Useful overrides:

```bash
GRAYLOG_VERSION=5.2.12 scripts/update-graylog-web.sh
GRAYLOG_REPOSITORY=https://github.com/Graylog2/graylog2-server.git scripts/update-graylog-web.sh
GRAYLOG_SOURCE_DIR=/path/to/graylog2-server scripts/update-graylog-web.sh
GRAYLOG_WEB_PREPARE_DEPS=false scripts/update-graylog-web.sh
```

Downloaded Graylog sources live under `.graylog/` by default. That directory is
ignored by Git.

## Unit Tests

Run backend unit tests:

```bash
make backend-test
```

Run one backend test class:

```bash
mvn -Dskip.web=true -Dtest=ObfuscationEngineTest test
```

Run frontend unit tests:

```bash
make frontend-test
```

The current frontend tests cover client-side validation in the configuration
page. Add focused frontend tests when changing `src/web`.

Backend regular expression rules are compiled with
[RE2/J](https://github.com/google/re2j). This is intentional: configuration is
administrator-provided input, and RE2/J gives linear-time matching for rules
used at message-processing time. Do not replace it with `java.util.regex` for
configured rules. When adding regex examples or smoke-test fixtures, avoid Java
regex features that RE2/J rejects, such as lookaround and backreferences.

## Full Package

Build the distributable plugin JAR:

```bash
make package
```

The JAR is written to:

```text
target/graylog-obfuscation-plugin-<version>.jar
```

## Version Updates

The plugin version is stored in both `pom.xml` and `package.json`. Use the
helper script when preparing a new plugin release:

```bash
scripts/set-plugin-version.sh 1.2.1
```

The script updates the Maven project version and the npm package version without
creating a Git tag.

Graylog versions are intentionally kept on the `5.x` line. Renovate is
configured in `renovate.json` to update:

- the Maven Graylog parent and server artifacts,
- `GRAYLOG_VERSION` in GitHub Actions,
- the Graylog Docker image tag used by the integration smoke test.

After a Graylog version update, run:

```bash
scripts/update-graylog-web.sh
yarn install
make backend-test
make frontend-test
make frontend-build
make smoke
```

## Integration Smoke Test

The smoke test starts a real Graylog container with MongoDB and OpenSearch,
mounts the built plugin JAR, and verifies backend and frontend integration.

Run:

```bash
make smoke
```

or:

```bash
scripts/graylog-smoke-test.sh
```

The script performs these checks:

1. Builds the plugin package.
2. Verifies that the JAR contains the frontend module manifest and JavaScript
   bundle.
3. Starts Docker Compose from `src/integration-test/graylog/docker-compose.yml`.
4. Waits for `http://127.0.0.1:19000/api/system/lbstatus`.
5. Calls `GET /api/system/plugins` and verifies that the obfuscation plugin is
   present.
6. Fetches the Graylog index page and verifies that it references the plugin UI
   bundle.
7. Fetches the plugin UI bundle from Graylog and verifies that it is the
   obfuscation plugin bundle.
8. Installs a minimal obfuscation configuration through the plugin REST API.
9. Calls `POST /api/plugins/com.netcracker.graylog2.plugin/obfuscation`.
10. Stops and removes the containers and volumes.

The default Graylog login used by the script is:

```text
admin / admin
```

Useful overrides:

```bash
GRAYLOG_HTTP_PORT=19000 scripts/graylog-smoke-test.sh
SMOKE_BUILD_TARGET=backend-package scripts/graylog-smoke-test.sh
```

Use `SMOKE_BUILD_TARGET=backend-package` only after `make package` has already
produced fresh frontend assets.

## Manual Local Graylog Run

Build the plugin first:

```bash
make package
```

Then start the integration Compose stack manually:

```bash
export PLUGIN_JAR="$PWD/target/graylog-obfuscation-plugin-1.2.0.jar"
export GRAYLOG_HTTP_PORT=19000
docker compose -p graylog-obfuscation-dev -f src/integration-test/graylog/docker-compose.yml up
```

Open `http://127.0.0.1:19000` and log in as `admin` / `admin`.

Stop the stack:

```bash
docker compose -p graylog-obfuscation-dev -f src/integration-test/graylog/docker-compose.yml down -v
```

## REST API Checks

List loaded plugins:

```bash
export GRAYLOG_AUTH='<user>:<password>'

curl -u "${GRAYLOG_AUTH}" -H 'X-Requested-By: local-dev' \
  http://127.0.0.1:19000/api/system/plugins
```

Test obfuscation:

```bash
curl -u "${GRAYLOG_AUTH}" -H 'X-Requested-By: local-dev' -H 'Content-Type: text/plain' \
  -X POST --data '123-12-1234' \
  http://127.0.0.1:19000/api/plugins/com.netcracker.graylog2.plugin/obfuscation
```

Expected response contains:

```json
{ "obfuscated_text": "********" }
```

## CI And Release

`.github/workflows/build.yml` runs:

- backend unit tests,
- frontend unit tests,
- full package build,
- Docker-based Graylog smoke test with UI bundle verification,
- artifact upload for the built JAR.

On tag pushes matching `v*`, the workflow also creates or updates the matching
GitHub Release and uploads the plugin JAR. The release artifact is the
integration point expected by downstream plugin packaging, including
`qubership-graylog-plugins-init`.

## Notes For Future Changes

- Keep the Graylog parent version, `graylog.version`, Docker image tag, and
  Graylog source checkout aligned.
- Backend-only changes should pass `make backend-test`.
- Frontend changes should pass `make frontend-test` and `make frontend-build`.
- Plugin registration, REST resources, dependency version changes, and UI wiring
  changes should pass `make smoke`.
- REST resources must use `javax.ws.rs` and `javax.validation` APIs for Graylog
  5.2.
