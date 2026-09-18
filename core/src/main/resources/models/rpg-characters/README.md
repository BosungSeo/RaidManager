# RPG Character Pack

- 제작자: Quaternius
- 출처: https://quaternius.com/packs/rpgcharacters.html
- 라이선스: CC0 1.0. 원본 LICENSE.txt 동봉.
- glTF 원본에 메시, 텍스처, 골격 애니메이션이 내장되어 있다.
- catalog.json에 원본 파일 경로, 애니메이션 이름, SHA-256, 다운로드 출처를 기록한다.
- 전투 연결: CharacterModels.kt. GameAssets가 지연 로딩과 리소스 해제를 담당한다.

| 캐릭터 | 모델 | 대기 | 이동 | 공격/스킬 |
| --- | --- | --- | --- | --- |
| AEGIS | Warrior | Idle_Weapon | Run_Weapon | Sword_Attack |
| LUNA | Cleric | Idle_Weapon | Run | Spell1 |
| ROOK | Rogue | Attacking_Idle | Run | Dagger_Attack |
| EMBER | Wizard | Idle_Weapon | Run_Weapon | Spell1 |
| NYX | Ranger | Idle_Weapon | Run_Holding | Bow_Shoot |
| MIRA | Monk | Idle_Attacking | Run | Attack2 |

공통 피격은 원본 이름 `RecieveHit`, 사망은 `Death`를 사용한다.
능력치, 사거리, 역할, 스킬 효과는 기존 게임 규칙을 유지한다.
Warrior 원본 장비는 검이며 방패 모델은 포함하지 않는다. 보호막은 기존 VFX로 표시한다.
MIRA는 수도사의 공격 동작과 기존 원거리 효과를 함께 사용한다.

## 검증

- 6종 × 5동작 OpenGL 렌더링 및 리소스 해제 통과.
- 실제 GameScene에서 두 팀으로 720프레임 전투 실행, 화면 합성과 GL 오류 없음 확인.

## 셰이더와 원본 표현

- 공식 팩 페이지와 다운로드 glTF 원본을 조사했다. 페이지에는 캐릭터별 전용 게임 셰이더 지정은 없다.
- 6종의 몸체·무기 재질 모두 `KHR_materials_unlit`이다. 따라서 모두 같은 텍스처 기반 Unlit 경로를 사용한다.
- gdx-gltf의 PBRShaderProvider는 해당 확장을 읽어 Unlit 셰이더 분기를 선택한다. 이름이 PBR이라고 해서 조명이 강제되는 것은 아니다.
- 원본 텍스처, 양면 재질, 골격 애니메이션을 유지한다. 금속 반사나 툰 명암을 추가하지 않는다.
- 캐릭터 전용 `originalUnlitColors` 설정으로 sRGB 디코딩과 근사 감마 인코딩을 함께 끈다.
  원본은 baseColorFactor가 흰색이고 정점 색상이 없으므로 RGBA8888 타깃에 텍스처 색을 그대로 복사할 수 있다.
  이 조건은 CharacterMaterialTest에서 검사한다. 일반 PBR 모델에는 이 설정을 사용하지 않는다.
- SpriteBatch 합성의 피격 색상과 사망 투명도는 게임 상태 연출로 유지한다.
- 참고: https://quaternius.com/packs/rpgcharacters.html
- Unlit 규격: https://github.com/KhronosGroup/glTF/tree/main/extensions/2.0/Khronos/KHR_materials_unlit
- 사용 라이브러리 설정: https://github.com/mgsx-dev/gdx-gltf/blob/2.3.0/gltf/src/net/mgsx/gltf/scene3d/shaders/PBRShaderConfig.java
