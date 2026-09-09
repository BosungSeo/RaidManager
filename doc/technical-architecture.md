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
       ├─ RaidSetupScene
       ├─ DungeonSelectScene
       ├─ GameScene (자동 전투)
       └─ BattleResultScene
```

`BattleSimulator`는 libGDX API에 의존하지 않는 고정 간격 전투 로직이며, 같은 편성과 던전에서 재현 가능한 `BattleResult`를 만든다. 전투 화면은 `BattleStage`를 통해 시뮬레이션 사건을 애니메이션으로 표현한다.

## Scene 규칙

- `Scene`은 `updateGame(delta)`와 `renderGame(batch)`를 제공한다.
- 현재 Scene이 입력을 받을 때만 `Gdx.input.inputProcessor`에 등록한다.
- 입력 이벤트에서는 명령을 큐에 넣고, 실제 상태 변경은 `updateGame`에서 처리한다.
- Scene 전환은 `RaidManagerGame`이 담당한다.
- `GameScene`은 `BattleSimulator`의 Snapshot과 CombatCue를 받아 전투 상태와 연출을 표시한다.
- `BattleStage`는 캐릭터와 보스의 위치, 대기 모션, 투사체, 피격, 회복, 보호막, 충격파를 담당한다.

## 리소스 관리

- 공유 텍스처와 폰트는 `GameAssets`에서 생성한다.
- 게임 종료 시 `GameAssets.dispose()`에서 리소스를 해제한다.
- 화면별 리소스가 필요해지면 생성·해제 시점을 명확히 정의한다.

## 확장 방향

- 게임 규칙을 Scene에서 분리해 순수 Kotlin 도메인 모델과 서비스로 이동한다.
- `BattleSimulator`는 전투 계산을 담당하고 `BattleLog`를 결과로 반환한다.
- `GameScene`은 시뮬레이터를 실행·표시하며 전투 규칙을 직접 소유하지 않는다.
- `GameState`는 플레이어 진행, 보유 캐릭터, 공격대 편성, 보상을 관리한다.
- 캐릭터·스킬·아이템·던전의 수치는 데이터 정의로 분리한다.
- 저장 가능한 게임 상태와 UI 상태를 분리한다.
- 화면 크기와 입력 장치 차이를 흡수하는 공통 좌표/입력 계층을 검토한다.
- 시스템 테스트가 가능하도록 순수 Kotlin 로직을 우선 분리한다.
- 연출은 `CombatCue`를 소비하는 방식으로 연결해 렌더링 코드가 게임 규칙에 의존하지 않게 한다.

## 현재 프로토타입 연출 구조

```text
BattleSimulator
  ├─ Snapshot      → HP, 보호막, 전투 시간, 승패 상태
  ├─ BattleResult  → 결과 화면 통계와 실패 분석
  └─ CombatCue     → BattleStage 애니메이션
```

`GameAssets`는 공용 폰트와 1픽셀 텍스처 외에 캐릭터 실루엣과 이펙트에 사용하는 원형 텍스처를 관리한다. 실제 캐릭터 스프라이트와 파티클 리소스는 후속 작업에서 교체할 수 있다.

## 권장 의존 방향

```text
Scene → Application/GameState → Domain Model
                              ↑
                         BattleSimulator
```

도메인 모델과 전투 시뮬레이터는 libGDX 렌더링 API에 의존하지 않아야 한다. 이를 통해 데스크톱 실행 없이도 전투 규칙을 단위 테스트할 수 있다.
