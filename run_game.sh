#!/bin/zsh

set -e

PROJECT_DIR="${0:A:h}"
cd "$PROJECT_DIR"

if [[ -x "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java" ]]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
elif [[ -z "${JAVA_HOME:-}" ]]; then
  echo "Java를 찾을 수 없습니다. Android Studio 또는 JDK를 설치해 주세요."
  exit 1
fi

if command -v gradle >/dev/null 2>&1; then
  GRADLE="$(command -v gradle)"
else
  GRADLE="$(find "$HOME/.gradle/wrapper/dists" -type f -path '*/bin/gradle' 2>/dev/null | sort -V | tail -1)"
fi

if [[ -z "$GRADLE" || ! -x "$GRADLE" ]]; then
  echo "Gradle을 찾을 수 없습니다. Android Studio에서 Gradle을 동기화하거나 Gradle을 설치해 주세요."
  exit 1
fi

echo "RaidManager를 실행합니다..."
echo "JAVA_HOME: $JAVA_HOME"
echo "Gradle: $GRADLE"
"$GRADLE" :lwjgl3:run
