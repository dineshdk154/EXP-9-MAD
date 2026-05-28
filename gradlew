#!/usr/bin/env sh
#
# Minimal Gradle wrapper script (POSIX).
# Why: keep repository self-contained and ensure Gradle 8+ wrapper split jars work in CI.
#

set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_SHARED_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper-shared.jar"
WRAPPER_CLI_JAR="$APP_HOME/gradle/wrapper/gradle-cli.jar"
WRAPPER_FILES_JAR="$APP_HOME/gradle/wrapper/gradle-files.jar"

# Newer Gradle versions split wrapper classes; include both when present.
CLASSPATH="$WRAPPER_JAR"
if [ -f "$WRAPPER_SHARED_JAR" ]; then
  CLASSPATH="$CLASSPATH:$WRAPPER_SHARED_JAR"
fi
# Gradle wrapper also depends on Gradle CLI classes (CommandLineParser) in newer distributions.
if [ -f "$WRAPPER_CLI_JAR" ]; then
  CLASSPATH="$CLASSPATH:$WRAPPER_CLI_JAR"
fi
# Wrapper install uses internal file locking APIs that live in gradle-files in Gradle 8.x.
if [ -f "$WRAPPER_FILES_JAR" ]; then
  CLASSPATH="$CLASSPATH:$WRAPPER_FILES_JAR"
fi

if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi

exec "$JAVACMD" ${JAVA_OPTS:-} ${GRADLE_OPTS:-} \
  "-Dorg.gradle.appname=gradlew" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain "$@"
