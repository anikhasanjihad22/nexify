#!/usr/bin/env bash
# Builds the Nexify debug APK on a fresh Ubuntu box (GitHub Codespaces works).
# Usage: bash scripts/build_nexify.sh [release]
set -euo pipefail

cd "$(dirname "$0")/.."
ROOT="$PWD"

SDK_DIR="${ANDROID_HOME:-$HOME/android-sdk}"
CMDLINE_TOOLS_ZIP="commandlinetools-linux-11076708_latest.zip"
NDK_VERSION="28.2.13676358"

java_major() {
  java -version 2>&1 | sed -n '1s/.*version "\([0-9]*\).*/\1/p'
}

if ! command -v java >/dev/null || [ "$(java_major)" -lt 17 ]; then
  echo "JDK 17 or newer is required (found: $(command -v java >/dev/null && java_major || echo none))." >&2
  exit 1
fi

if [ ! -x "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" ]; then
  mkdir -p "$SDK_DIR/cmdline-tools"
  curl -fsSL -o /tmp/cmdline-tools.zip "https://dl.google.com/android/repository/$CMDLINE_TOOLS_ZIP"
  unzip -q -o /tmp/cmdline-tools.zip -d "$SDK_DIR/cmdline-tools"
  rm -rf "$SDK_DIR/cmdline-tools/latest"
  mv "$SDK_DIR/cmdline-tools/cmdline-tools" "$SDK_DIR/cmdline-tools/latest"
  rm /tmp/cmdline-tools.zip
fi

export ANDROID_HOME="$SDK_DIR"
export PATH="$SDK_DIR/cmdline-tools/latest/bin:$PATH"

yes | sdkmanager --licenses >/dev/null || true
sdkmanager --install \
  "platform-tools" \
  "platforms;android-36" \
  "build-tools;36.0.0" \
  "cmake;3.22.1" \
  "ndk;$NDK_VERSION"

echo "sdk.dir=$SDK_DIR" > local.properties

if [ "${1:-}" = "release" ]; then
  TASK=":app:assembleStandardRelease"
  OUT="app/build/outputs/apk/standard/release"
else
  TASK=":app:assembleStandardDebug"
  OUT="app/build/outputs/apk/standard/debug"
fi

./gradlew --no-daemon --no-configuration-cache \
  -Dorg.gradle.jvmargs="-Xmx5g -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8" \
  -Dkotlin.compiler.execution.strategy=in-process \
  -PallowDebugSignedRelease=true \
  "$TASK"

mkdir -p dist
cp "$ROOT/$OUT"/*.apk dist/
echo
echo "APK ready:"
ls -lh dist/*.apk
