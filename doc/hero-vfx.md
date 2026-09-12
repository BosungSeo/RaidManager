# 캐릭터 기술 스프라이트

- 파일: `core/src/main/resources/sprites/hero-vfx.png`
- 내장 ImageGen으로 생성한 3열 × 2행 아틀라스. 검정 배경을 발광 합성한다.
- 순서: GUARD(청색 보호막), HEAL(녹색·금색 회복), STRIKE(금색 참격), CLEAVE(화염), INTERRUPT(보라 번개), SUNDER(붉은 저주).
- CombatCue의 실제 대상 위치에서 확대·상승·하강·페이드 애니메이션을 재생한다.
- 보호막은 아군 각각, 광역기는 생존 몬스터 각각에 표시한다. SUNDER의 피해·상태 중복 이벤트는 한 번만 그린다.
- 프레임 교체 애니메이션이 아닌 기술별 정적 스프라이트의 변형 애니메이션이다.

## 생성 프롬프트

Use case: stylized-concept. Production game VFX atlas, exactly THREE columns TWO rows, 1536x1024 landscape. Six isolated hand-painted dark fantasy luminous spell effects, all on absolutely pure black RGB(0,0,0) for additive blending. Each centered in an equal square cell with generous 60px black gutters, no borders, letters, characters or environment. Read order: top left cyan spectral shield with hexagonal luminous rim; top middle mint and gold healing spiral with upward motes; top right golden double sword slash and sparks; bottom left orange-red swirling fire explosion; bottom middle violet lightning star burst for disruption; bottom right crimson-violet broken rune curse with descending wisps. Keep centers partially empty so characters remain readable through effects. Soft glows fade fully to BLACK before cell edges. Distinct six silhouettes. No checkerboard, no gray background, no actual swords or people.
