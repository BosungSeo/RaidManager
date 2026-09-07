# RaidManager 작업 메모

## 프로젝트 개요

- Kotlin 기반 libGDX 게임 프로젝트
- Android와 macOS 데스크톱을 함께 지원
- `libGDK`로 요청했으나 일반적으로 사용되는 `libGDX`로 구성
- Gradle Kotlin DSL 사용

## 프로젝트 구조

```text
RaidManager/
├── android/                  # Android 런처 및 앱 설정
├── core/                     # Android/macOS 공통 게임 코드
│   └── src/main/
│       ├── kotlin/com/raidmanager/game/RaidManagerGame.kt
│       └── resources/Title.png
├── lwjgl3/                   # macOS 데스크톱 런처
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## 현재 구현된 화면

1. `Title.png`를 화면에 맞춰 비율 유지하며 중앙 표시
2. 화면 왼쪽 중상단에 `RaidManager` 텍스트를 잠시 표시
3. 타이틀 텍스트는 약 3초 후 페이드아웃
4. 타이틀 이미지는 계속 유지
5. 타이틀 연출 이후 이미지 위에 다음 버튼 두 개를 오버레이
   - `NEW GAME`
   - `LOAD GAME`

현재 버튼은 화면 표시만 구현되어 있으며 실제 게임 시작/저장 불러오기 동작은 아직 연결되지 않았습니다.

## macOS 실행 방법

Android Studio 내장 JDK를 사용합니다.

```bash
cd "/Users/bosung/Project/개발프로젝트/RaidManager"
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
"$HOME/.gradle/wrapper/dists/gradle-9.4.1-bin/arn2x92ynaizyzdaamcbpbhtj/gradle-9.4.1/bin/gradle" :lwjgl3:run
```

macOS GLFW 실행을 위해 `lwjgl3/build.gradle.kts`에 `-XstartOnFirstThread` 옵션이 설정되어 있습니다.

## 빌드

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
"$HOME/.gradle/wrapper/dists/gradle-9.4.1-bin/arn2x92ynaizyzdaamcbpbhtj/gradle-9.4.1/bin/gradle" :lwjgl3:build
```

Android APK 빌드:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
"$HOME/.gradle/wrapper/dists/gradle-9.4.1-bin/arn2x92ynaizyzdaamcbpbhtj/gradle-9.4.1/bin/gradle" :android:assembleDebug
```

## 다음 작업 후보

- 버튼 터치/마우스 클릭 이벤트 연결
- `NEW GAME` 클릭 시 게임 플레이 화면 전환
- `LOAD GAME` 클릭 시 저장 데이터 불러오기
- 버튼 스타일과 폰트 개선
- Android에서 한글 폰트 리소스 추가
- 화면 해상도별 레이아웃 조정
