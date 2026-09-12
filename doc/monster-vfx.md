# 몬스터 기술 이펙트

- 이미지: `core/src/main/resources/sprites/monster-vfx.png`
- 내장 ImageGen으로 생성한 2×2 아틀라스. 위쪽부터 충격파, 곤충 떼, 회복, 차단 순서.
- 투명 배경 요청 결과에 체크무늬가 포함되어 검정 배경으로 재생성했다. 런타임에서 발광 합성하며 원본 알파 이미지가 아니다.
- GameAssets에서 로딩·해제하고 BattleStage에서 시전 상태와 CombatCue에 맞춰 확대·이동·페이드한다.
- 시전 중 기술명과 게이지 표시. 전투 종료 시 시전 효과를 숨긴다.
- 몬스터별 ID와 위치에 맞춰 기술 이벤트를 재생하며, 각 개체에 체력·시전 게이지를 표시한다.

## 개별 전투 동작

- 각 몬스터가 HP, 공격 타이머, 시전·쿨다운, 회복 감소 상태를 따로 보관한다.
- 일반 공격은 첫 생존 몬스터에 집중하고, 처치하면 다음 대상으로 전환한다. CLEAVE는 모든 생존 적에게 적용한다.
- 동일 종류도 시작 시점과 공격 간격을 조금씩 다르게 두며, 시전 중에는 기본 공격을 쉰다.
- 인터럽트 준비 효과 하나는 시전 중인 한 몬스터만 취소한다. 사망한 몬스터는 시전·공격·회복하지 않는다.
- 영웅은 기존 고유 공격 간격을 유지하고 ID별 첫 공격·스킬 지연을 적용한다. 무작위가 없어 같은 편성은 재현 가능하다.
- 다수 전투는 실제 적의 공격 횟수도 늘어나므로 이전의 공유 체력 방식보다 어렵다.
- 시각 검증: `RAID_PREVIEW_MONSTERS=3 ./gradlew :lwjgl3:spritePreview`.

## 생성 프롬프트

Use case: stylized-concept. Create a production 1024x1024 transparent PNG VFX sprite atlas for a painted dark fantasy RPG. Exact 2 by 2 equal grid, no borders or text, each effect entirely inside its own 512 square cell with 40px transparent padding. Top left: orange circular ground shockwave with jagged rock debris and glowing cracks, perspective ellipse. Top right: purple magical swarm of many tiny winged insects flying left with violet trails. Bottom left: emerald healing fountain with upward wisps, sparkling motes and a circular aura. Bottom right: violet shattered magic circle with sharp luminous fragments, interrupt burst. Truly transparent background with alpha, no checkerboard, no environment, no characters. Each isolated effect centered in its cell, soft luminous edges. This is one four-effect atlas, not a scene.

## 배경 수정 프롬프트

Edit this four-effect atlas. Remove EVERY checkerboard pixel and replace background with uniform pure BLACK RGB 0,0,0 including gaps and behind translucent glows. Preserve exact 2x2 grid and four colored effects and positions. This will be used with additive blending. No gray background anywhere. Pure black margins and empty spaces. Keep orange impact, violet insect swarm, green healing fountain, violet shattered circle.
