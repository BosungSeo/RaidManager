# 던전 전투 배경

내장 ImageGen으로 몬스터별 배경을 각각 생성했다. 전투 캐릭터와 UI는 이미지에 포함하지 않고 기존 렌더링을 유지한다.

| 던전 ID | 배경 | 리소스 |
| --- | --- | --- |
| colossus | 고대 철 주조소, 용광로·쇠사슬·청동 기어 | `core/src/main/resources/backgrounds/colossus.png` |
| swarm | 거미줄과 알주머니가 있는 보랏빛 동굴 | `core/src/main/resources/backgrounds/swarm.png` |
| slime | 뿌리와 수정, 수원이 흐르는 지하 유적 | `core/src/main/resources/backgrounds/slime.png` |

GameAssets에서 한 번 로드하고 dispose한다. BattleStage는 dungeon.id로 배경을 선택해 1280 × 720 논리 화면에 맞춰 그린다. 기존 격자 바닥과 상시 보스 원형 장식은 제거했고, 상·하단 반투명 패널로 UI 대비를 확보했다. 배경은 정적 이미지이며 기존 부유 입자와 전투 이펙트는 유지한다.

검증: `./gradlew :core:test :lwjgl3:build :lwjgl3:spritePreview`. 기존 시각 검증 도구가 세 던전의 실제 전투 장면을 `/tmp/raid-monsters-{colossus,swarm,slime}-{frame}.png`로 저장한다.

## 생성 프롬프트

### colossus

```text
Use case: stylized-concept
Asset type: production 2D RPG battle background, a single landscape 16:9 image, 1536x864.
Style: detailed hand-painted semi-realistic dark fantasy, matching painted realistic hero and monster sprites.
Composition: empty arena viewed slightly downward from a fixed side-on three-quarter game camera. Broad continuous walkable ground fills the lower 65 percent. Distant architecture occupies upper third. Heroes will be composited at x20-35 percent, y40-70 percent; boss at x75 percent,y55 percent, so keep these areas uncluttered and without foreground obstructions. No central pedestal. Dark low-contrast ground for readable character silhouettes. Upper 20 percent and bottom 20 percent subdued for text overlays. Atmospheric depth with environmental detail concentrated along far edges.
Constraints: Environment ONLY, NO characters, monsters, creatures, silhouettes, UI, text, labels, logos, borders, grids, watermark. No foreground objects blocking the arena. Entire canvas finished, opaque image.
Scene: IRON COLOSSUS's ancient subterranean foundry. Massive weathered iron pillars, bronze gears and hanging chains at the back and edges, distant furnace openings with restrained amber glow, dim cyan energy conduits in stone walls. Wide worn iron-and-basalt floor, subtle seams and soot. Steel blue shadows, bronze details, tiny floating embers. Epic abandoned industrial sanctuary, not modern factory. Keep the center floor clear.
```

### swarm

```text
Use case: stylized-concept
Asset type: production 2D RPG battle background, a single landscape 16:9 image, 1536x864.
Style: detailed hand-painted semi-realistic dark fantasy, matching painted realistic hero and monster sprites.
Composition: empty arena viewed slightly downward from a fixed side-on three-quarter game camera. Broad continuous walkable ground fills the lower 65 percent. Distant architecture occupies upper third. Heroes will be composited at x20-35 percent, y40-70 percent; boss at x75 percent,y55 percent, so keep these areas uncluttered and without foreground obstructions. No central pedestal. Dark low-contrast ground for readable character silhouettes. Upper 20 percent and bottom 20 percent subdued for text overlays. Atmospheric depth with environmental detail concentrated along far edges.
Constraints: Environment ONLY, NO characters, monsters, creatures, silhouettes, UI, text, labels, logos, borders, grids, watermark. No foreground objects blocking the arena. Entire canvas finished, opaque image.
Scene: BROODMOTHER's webbed cavern nest. Vaulted jagged charcoal stone chamber, layered spider silk attached to ceiling and outer walls, clusters of pale egg sacs recessed at far edges, distant misty violet cave passage. Broad dry dark slate floor, sparse fine web strands near outer margins only. Muted purple and cold blue atmospheric light, unsettling organic shapes. No spiders or insects; empty arena. Keep the center floor clear.
```

### slime

```text
Use case: stylized-concept
Asset type: production 2D RPG battle background, a single landscape 16:9 image, 1536x864.
Style: detailed hand-painted semi-realistic dark fantasy, matching painted realistic hero and monster sprites.
Composition: empty arena viewed slightly downward from a fixed side-on three-quarter game camera. Broad continuous walkable ground fills the lower 65 percent. Distant architecture occupies upper third. Heroes will be composited at x20-35 percent, y40-70 percent; boss at x75 percent,y55 percent, so keep these areas uncluttered and without foreground obstructions. No central pedestal. Dark low-contrast ground for readable character silhouettes. Upper 20 percent and bottom 20 percent subdued for text overlays. Atmospheric depth with environmental detail concentrated along far edges.
Constraints: Environment ONLY, NO characters, monsters, creatures, silhouettes, UI, text, labels, logos, borders, grids, watermark. No foreground objects blocking the arena. Entire canvas finished, opaque image.
Scene: PRIME SLIME's underground renewing grotto. Ancient moss-covered stone ruins embraced by roots, shallow emerald mineral pools along outer edges, delicate luminous green mushrooms and cyan crystals in distant alcoves, gentle low mist. Broad damp flat stone arena with subtle water reflections but no pools in combat standing areas. Deep teal shadows, restrained emerald and amber highlights. Lush mysterious alchemical spring, no slime creatures or blobs. Keep the center floor clear.
```

