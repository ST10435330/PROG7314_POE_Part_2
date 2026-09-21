#!/bin/sh
# Small wrapper launcher. The official Gradle wrapper JAR is downloaded once and verified.
set -eu
STUDYSYNC_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
STUDYSYNC_JAR="$STUDYSYNC_ROOT/gradle/wrapper/gradle-wrapper.jar"
STUDYSYNC_HASH=2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046
if [ ! -f "$STUDYSYNC_JAR" ]; then
    curl --fail --location --retry 2 --connect-timeout 20 --max-time 180 \
      https://raw.githubusercontent.com/gradle/gradle/v8.11.1/gradle/wrapper/gradle-wrapper.jar \
      --output "$STUDYSYNC_JAR.download"
    if command -v sha256sum >/dev/null 2>&1; then
        STUDYSYNC_ACTUAL=$(sha256sum "$STUDYSYNC_JAR.download" | cut -d ' ' -f 1)
    else
        STUDYSYNC_ACTUAL=$(shasum -a 256 "$STUDYSYNC_JAR.download" | cut -d ' ' -f 1)
    fi
    [ "$STUDYSYNC_ACTUAL" = "$STUDYSYNC_HASH" ] || { echo 'Gradle wrapper checksum mismatch.' >&2; exit 1; }
    mv "$STUDYSYNC_JAR.download" "$STUDYSYNC_JAR"
fi
STUDYSYNC_JAVA=java
if [ -n "${JAVA_HOME:-}" ]; then STUDYSYNC_JAVA="$JAVA_HOME/bin/java"; fi
exec "$STUDYSYNC_JAVA" -classpath "$STUDYSYNC_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
