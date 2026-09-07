# RaidManager 에이전트 작업 지침

## 프로젝트 개요

- Kotlin 기반 libGDX 게임 프로젝트
- 공통 게임 코드는 `core` 모듈에 둔다.
- Android 실행 코드는 `android`, 데스크톱 실행 코드는 `lwjgl3`에 둔다.
- 현재 화면 흐름은 `IntroScene` → `MainMenuScene` → `GameScene`이다.

## 작업 원칙

- 사용자의 요청 범위를 먼저 확인하고, 요청하지 않은 대규모 구조 변경은 피한다.
- 화면 상태와 전환은 `core/src/main/kotlin/com/raidmanager/game/scene`의 Scene 구조를 따른다.
- 리소스 생성과 해제는 `GameAssets`에서 관리한다.
- 입력 처리는 각 Scene의 `InputAdapter`에 두고, 상태 변경은 명령 큐를 거쳐 `updateGame`에서 처리한다.
- 렌더링 좌표는 libGDX의 좌측 하단 원점을 기준으로 작성한다.
- 공통 게임 로직에 플랫폼 전용 API를 직접 넣지 않는다.
- 기존 파일의 사용자 변경 내용을 보존하고, 필요한 부분만 작게 수정한다.
- Kotlin 파일은 기존 스타일을 따르고 한 줄 길이는 140자 이내를 우선한다.

## 주요 파일

- `core/src/main/kotlin/com/raidmanager/game/RaidManagerGame.kt`: 게임 초기화와 Scene 전환
- `core/src/main/kotlin/com/raidmanager/game/GameAssets.kt`: 공유 텍스처·폰트 리소스
- `core/src/main/kotlin/com/raidmanager/game/scene/IntroScene.kt`: 인트로 화면
- `core/src/main/kotlin/com/raidmanager/game/scene/MainMenuScene.kt`: 메뉴와 버튼 입력
- `core/src/main/kotlin/com/raidmanager/game/scene/GameScene.kt`: 게임 화면
- `core/src/main/resources/Title.png`: 타이틀 이미지

## 실행 및 검증

macOS 데스크톱 실행:

```bash
./gradlew :lwjgl3:run
```

Android 디버그 APK 빌드:

```bash
./gradlew :android:assembleDebug
```

변경 후에는 가능한 범위에서 다음 순서로 검증한다.

1. 변경된 Kotlin 파일의 컴파일 오류와 import를 확인한다.
2. `./gradlew :lwjgl3:build`를 실행한다.
3. 입력·화면 전환을 변경했다면 데스크톱 실행으로 직접 확인한다.
4. Android 관련 코드를 변경했다면 `./gradlew :android:assembleDebug`도 실행한다.

## 현재 알려진 미완성 기능

- `LOAD GAME`은 저장 데이터를 읽지 않고 `NO SAVE DATA`만 표시한다.
- `GameScene`은 프로토타입 화면이며 실제 레이드 플레이 로직은 없다.
- 버튼은 기능 중심의 임시 스타일이다.
- 한글 폰트 리소스와 해상도별 세밀한 레이아웃 조정이 필요하다.

## 멀티에이전트 작업 규칙

역할별 지침은 `agents/` 폴더에 둔다. 오케스트레이터는 작업을 분해하고, 아래 파일 소유권이 겹치지 않을 때만 에이전트를 병렬로 실행한다.

- `game-designer`: `doc/**`의 게임 기획 및 시스템 설계
- `developer`: 게임 로직, Scene, UI 연결 등 코드 구현
- `qa-reviewer`: 코드 수정 없이 빌드, 실행, 테스트와 구조 검토

작업 순서는 기본적으로 다음을 따른다.

1. `game-designer`가 관련 문서와 규칙을 확정한다.
2. `developer`가 게임 상태·시스템을 구현하고 Scene에 연결한다.
3. `qa-reviewer`가 빌드와 실행을 검증하고 문제를 보고한다.

모든 기록과 보고는 짧고 명료하게 작성한다. 에이전트 간 결과에는 꼭 필요한 변경 파일, 결정 사항, 미해결 항목, 검증 결과만 포함한다. 서로 다른 에이전트가 같은 파일을 동시에 수정하지 않으며, 규칙 변경 없이 UI 코드에 게임 로직을 복제하지 않는다.
