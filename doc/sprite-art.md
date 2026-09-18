# 캐릭터 스프라이트

## 현재 전투 캐릭터

- 영웅 6종은 Quaternius RPG Character Pack의 외부 glTF 모델과 원본 골격 애니메이션을 사용한다.
- 모델별 연결은 `graphics/CharacterModels.kt`, 공통 렌더링은 `graphics/ExternalModelRenderer.kt`에 둔다.
- 모델·라이선스·동작 목록은 [RPG 캐릭터 에셋](../core/src/main/resources/models/rpg-characters/README.md)을 참고한다.
- 기존 스프라이트와 코드 생성 탱커는 비교용 리소스로 보존한다.

## 이전 탱커 3D 프로토타입 (비교용)

- 이전 전투 화면의 `Role.TANK`(AEGIS)는 `graphics/TankModel.kt`에서 생성하는 관절형 3D 메시로 표시한다.
- 강철 갑옷, 금색 테두리, 검, 방패, 푸른 망토를 가진 스타일화된 모델이며 외부 모델 파일이나 추가 라이브러리는 없다.
- 원본 `aegis.png`의 16개 자세를 3D 관절 포즈로 재현한다. 이미지 기반 원본이므로 관절 궤적이 완전히 동일한 것은 아니다.
- `SpriteTimeline`의 프레임 선택·우선순위·타이밍, 전장의 이동·전진·반동·호흡·사망 회전·투명도는 공유한다.
- `TankModelRenderer`가 깊이 버퍼와 조명으로 투명 타깃에 렌더링하고, `BattleStage`가 기존 발 기준과 정렬 순서로 합성한다.
- 모델·렌더러·프레임버퍼는 `GameAssets`가 필요할 때 생성하고 종료 시 해제한다. 다른 영웅과 몬스터는 기존 스프라이트를 사용한다.
- `RAID_PREVIEW_TANK=true ./gradlew :lwjgl3:spritePreview`로 원본/3D의 16개 자세를 비교한다.
  결과는 `/tmp/raid-tank-poses-right.png`, `/tmp/raid-tank-poses-left.png`이며 OpenGL 오류도 검사한다.

## 리소스와 제작 방식

- 생성 방식: 내장 ImageGen, 반실사 판타지 캐릭터 아틀라스.
- 파일: `core/src/main/resources/sprites/raid-heroes.png`.
- 887 × 1774 PNG, 3열 × 6행. 열은 대기 / 공격·시전 / 피격 순서, 행은 AEGIS / LUNA / ROOK / EMBER / NYX / MIRA 순서다.
- 원본은 녹색 배경 RGB 이미지다. GameAssets의 heroShader가 렌더링할 때 녹색을 투명하게 처리한다.
- 생성 이미지의 셀 간격 차이를 보정하도록 TextureRegion 영역과 발 기준점을 명시했다.
- 리소스 생성·해제는 GameAssets가 담당한다.

## 동작 연결

- CombatCue의 source로 공격·시전 포즈를 0.5초 유지한다. 회복·보호막 시전자도 포함한다.
- HIT의 target은 피격 포즈를 0.24초 표시하며 공격·시전보다 우선한다.
- Tank와 ROOK은 공격 전진, 나머지는 시전 기울기를 적용한다.
- 대기는 발을 기준으로 호흡 스케일, 피격은 반동과 붉은 틴트, 사망은 피격 포즈에 회전·투명도를 적용한다.
- 포즈 3장을 이용한 애니메이션이다. 연속 보행 프레임이나 관절 리깅은 아직 없다.
- 기존 스킬별 이펙트는 별도로 유지하며 전투 규칙을 바꾸지 않는다.

## 생성 프롬프트

내장 ImageGen을 사용했다. 최초 서버 오류 후 해상도를 낮춰 재시도했으며, 체크무늬 배경이 포함된 초안에 다음 편집 지시를 적용했다:

> Preserve exactly all six character identities and the 3 poses per character. Replace ALL checkerboard background with genuinely transparent alpha pixels. If alpha output is unsupported, use a perfectly flat solid pure RGB(0,255,0) green background. Center each sprite in its equal-sized cell, including all weapons. No overlap, borders, labels, scenery or floor. Portrait 1:2 aspect ratio.

```text
Use case: stylized-concept
Asset type: production 2D RPG sprite atlas, ONE PNG sheet with genuine transparent alpha background.
Create a precise 3-column by 6-row equal-cell sprite atlas, portrait 1:2 aspect ratio, 1024x2048 pixels. No text, labels, borders, floor, shadow plane, scenery or checkerboard pixels. Each cell contains exactly ONE complete full-body character including weapons, centered within its cell with generous 12% clear padding. Feet baseline same relative position in each cell. All face RIGHT in side/three-quarter view, consistent orthographic camera and size. No cropping or overlapping cells.
Style: high-quality hand-painted semi-realistic dark fantasy RPG units, realistic adult proportions (not chibi), readable silhouettes, detailed brushed metal, cloth folds and leather, restrained high contrast lighting for a dark game battlefield.
Columns left to right: neutral combat idle, strong attack/cast pose toward right, recoiling hurt pose. Maintain identical identity, equipment, proportions and palette across each row.
Rows top to bottom:
1 AEGIS male heavily armored knight, steel plate, blue tabard, broad shield and short sword. Attack column shield thrust.
2 LUNA female healer, ivory and emerald robes, silver staff topped by green crystal, hood lowered. Cast column staff forward and free hand extended.
3 ROOK male swordsman, brown leather with amber cloth and steel pauldrons, two-handed sword. Attack column forceful forward slash with entire sword within cell.
4 EMBER female fire mage, crimson and charcoal robes, copper staff and orange crystal. Cast column raised arm casting fire, small contained orange glow.
5 NYX female hooded spellblade, dark violet leather, short dagger and violet talisman. Cast column talisman forward.
6 MIRA male debuffer warlock, teal and plum layered robes, rune book and dark staff. Cast column book open and staff pointing right.
Exactly 18 distinct isolated sprites, no additional figures. Preserve real transparent background; keep all magical light close to the character and within cell. This sheet will be split into equal grid TextureRegions in a libGDX game.
```

## 시각 검증 실행

`./gradlew :lwjgl3:spritePreview`는 실제 GameScene을 두 편성으로 실행하고 `/tmp/raid-sprites-0.png`, `/tmp/raid-sprites-1.png`를 저장한다. 이어서 아래의 몬스터 검증을 실행한 뒤 종료한다.

## 몬스터 스프라이트

- 파일: `core/src/main/resources/sprites/raid-monsters.png` (1254 × 1254 RGB PNG).
- 내장 ImageGen으로 생성한 3종 × 3포즈 아틀라스. 행은 COLOSSUS / BROODMOTHER / PRIME SLIME, 열은 대기 / 공격 / 피격이다.
- 왼쪽의 영웅을 향하도록 제작했다. 셀 경계와 발·바닥 기준점은 GameAssets의 monsterFrames에서 보정한다.
- 체크무늬 초안을 자홍색 배경으로 수정했다. monsterShader가 배경을 제거하며, 영웅의 녹색 크로마키와 분리하여 슬라임의 녹색을 보존한다. 원본 자체는 투명 PNG가 아니다.
- MonsterAnimation은 전투 이벤트만 소비한다. 일반 공격·광역기는 공격 포즈와 전진, 실제 피해는 피격 포즈·틴트, 차단은 공격 취소와 움찔, 재생은 녹색 파동으로 연결한다.
- 공격 중 일반 피격은 포즈를 가리지 않고 틴트로 표시한다. 차단은 공격보다 우선한다. 보스 사망은 피격 포즈를 0.7초간 납작하게 만들고 흐리게 한다.
- 거인은 무거운 강타, 거미는 더 긴 돌진, 슬라임은 가로·세로 탄성 변형을 사용한다. 보행 리깅이나 연속 프레임 애니메이션은 아직 없다.
- 테스트: `MonsterAnimationTest`에서 공격 종료, 피해/공격 우선순위, 차단, 광역기, 재생, 사망, 전투 종료를 검증한다.
- `./gradlew :lwjgl3:spritePreview`는 기존 영웅 검증 후 보스 3종도 실행하고 `/tmp/raid-monsters-{colossus,swarm,slime}-{frame}.png`에 전투 초반·공격·특수기·종료 시점 화면을 저장한다.

### 몬스터 생성 프롬프트

```text
Use case: stylized-concept
Asset type: one production 2D RPG monster sprite atlas PNG for a libGDX game.
Create a 1536x1536 square sheet with EXACTLY 3 equal columns and 3 equal rows, exactly nine isolated full-body sprites. Background must be genuinely transparent alpha; if alpha output is unsupported, use a perfectly flat pure RGB(255,0,255) magenta background, no checkerboard. No floor, cast shadows, scenery, borders, labels, text or watermark.
Style: hand-painted semi-realistic dark fantasy, detailed materials, readable silhouette, orthographic three-quarter side view. ALL monsters face LEFT to attack heroes standing on the left of the battlefield. Consistent identity, palette and scale across each row. Each sprite centered in its 512x512 cell with at least 12% margin on every side; ALL legs, limbs and effects fit inside the cell. Feet/base on same baseline at 86% cell height.
Columns: 1 idle ready pose; 2 dynamic attacking pose toward LEFT; 3 recoiling hurt pose, leaning away toward right, still full-body.
Row 1: IRON COLOSSUS, massive ancient iron golem, weathered steel armor plates, bronze joints, bright cyan circular chest reactor and eyes. Idle heavy grounded fists. Attack leftward downward hammer-fist slam with one arm. Hurt crumpled shoulders and dim core.
Row 2: BROODMOTHER, monstrous queen spider, eight articulated legs, dark obsidian chitin and muted violet abdomen, red eyes and ivory fangs. Idle low poised body. Attack leftward lunge with front legs and fangs extended. Hurt withdrawn legs and tilted abdomen. Entire leg silhouette contained within each cell.
Row 3: PRIME SLIME, large translucent emerald-green slime monster with visible amber inner core, glossy surface and suspended bubbles. Idle rounded asymmetric dome. Attack one thick gelatinous pseudopod thrust toward LEFT. Hurt compressed flattened wobbling blob. No magenta or pink in any monster.
Exactly nine sprites. No additional creatures or detached effects. Keep lighting restrained and consistent, suitable for a dark navy battle stage. Realistic monster materials, not cartoon emoji, not chibi.
```

### 배경 수정 프롬프트

```text
Edit target: the supplied monster sprite atlas. Preserve all nine monster sprites, their identities, materials, colors, facing left, and exact poses. Change ONLY the checkerboard background to a completely uniform pure RGB(255,0,255) magenta field. Remove every checkerboard pixel including between legs. No pink reflected light on the monsters. Preserve green translucent slime as opaque painted green, no checkerboard visible through it. Maintain a 3-column by 3-row grid, add sufficient empty magenta padding so each full sprite fits its own cell without touching adjacent cells. No labels, borders, ground, shadow or extra creatures. Square image.
```
