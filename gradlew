#!/usr/bin/env sh
set -eu
ROOT="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
if command -v gradle >/dev/null 2>&1; then exec gradle -p "$ROOT" "$@"; fi
VERSION=8.6
CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}/mykingdom-bootstrap/gradle-$VERSION"
if [ ! -x "$CACHE/bin/gradle" ]; then
  mkdir -p "$(dirname "$CACHE")"
  TMP="$(mktemp -d)"
  trap 'rm -rf "$TMP"' EXIT
  if command -v curl >/dev/null 2>&1; then curl -fL "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -o "$TMP/gradle.zip"
  else echo 'Install Gradle 8.6 or curl to bootstrap Gradle.' >&2; exit 1; fi
  if command -v unzip >/dev/null 2>&1; then unzip -q "$TMP/gradle.zip" -d "$TMP"
  else echo 'Install unzip to bootstrap Gradle.' >&2; exit 1; fi
  mv "$TMP/gradle-$VERSION" "$CACHE"
fi
exec "$CACHE/bin/gradle" -p "$ROOT" "$@"
