# 기술 구조

## 모듈 구조

- `core`: Android와 macOS에서 공유하는 게임 코드와 리소스
- `android`: Android 런처 및 앱 설정
- `lwjgl3`: macOS 데스크톱 런처

## 현재 실행 흐름

`RaidManagerGame`이 공통 진입점이며 현재 Scene을 보관한다.

```text
RaidManagerGame
  ├─ GameAssets
  └─ currentScene: Scene
       ├─ IntroScene
       ├─ MainMenuScene
       └─ GameScene
```

## Scene 규칙

- `Scene`은 `updateGame(delta)`와 `renderGame(batch)`를 제공한다.
- 현재 Scene이 입력을 받을 때만 `Gdx.input.inputProcessor`에 등록한다.
- 입력 이벤트에서는 명령을 큐에 넣고, 실제 상태 변경은 `updateGame`에서 처리한다.
- Scene 전환은 `RaidManagerGame`이 담당한다.

## 리소스 관리

- 공유 텍스처와 폰트는 `GameAssets`에서 생성한다.
- 게임 종료 시 `GameAssets.dispose()`에서 리소스를 해제한다.
- 화면별 리소스가 필요해지면 생성·해제 시점을 명확히 정의한다.

## 확장 방향

- 게임 규칙을 Scene에서 분리해 도메인 모델로 이동한다.
- 저장 가능한 게임 상태와 UI 상태를 분리한다.
- 화면 크기와 입력 장치 차이를 흡수하는 공통 좌표/입력 계층을 검토한다.
- 시스템 테스트가 가능하도록 순수 Kotlin 로직을 우선 분리한다.
