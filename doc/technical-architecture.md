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
       └─ GameScene (현재 프로토타입)

향후 Scene 흐름:

```text
MainMenuScene
  → RaidSetupScene
  → DungeonSelectScene
  → BattleScene
  → BattleResultScene
```
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

- 게임 규칙을 Scene에서 분리해 순수 Kotlin 도메인 모델과 서비스로 이동한다.
- `BattleSimulator`는 전투 계산을 담당하고 `BattleLog`를 결과로 반환한다.
- `BattleScene`은 시뮬레이터를 실행·표시하며 전투 규칙을 직접 소유하지 않는다.
- `GameState`는 플레이어 진행, 보유 캐릭터, 공격대 편성, 보상을 관리한다.
- 캐릭터·스킬·아이템·던전의 수치는 데이터 정의로 분리한다.
- 저장 가능한 게임 상태와 UI 상태를 분리한다.
- 화면 크기와 입력 장치 차이를 흡수하는 공통 좌표/입력 계층을 검토한다.
- 시스템 테스트가 가능하도록 순수 Kotlin 로직을 우선 분리한다.

## 권장 의존 방향

```text
Scene → Application/GameState → Domain Model
                              ↑
                         BattleSimulator
```

도메인 모델과 전투 시뮬레이터는 libGDX 렌더링 API에 의존하지 않아야 한다. 이를 통해 데스크톱 실행 없이도 전투 규칙을 단위 테스트할 수 있다.
