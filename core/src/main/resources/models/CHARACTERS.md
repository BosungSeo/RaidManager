# 캐릭터 에셋 라이브러리

기존 Quaternius RPG 6종과 앞서 소개한 세 팩의 무료 배포본을 등록한다.
캐릭터별 능력치를 새로 만들거나 모든 모델을 전투 편성에 추가한 것은 아니다.

- KayKit Adventurers Free 2.0: 캐릭터 5종 + Rogue_Hooded 변형 1개, 장비 31개, 기본 애니메이션 파일 2개.
- Kenney Blocky Characters 2.0: 캐릭터 18종.
- Quaternius Modular Fantasy Standard: 의상 4개, 조립 부품 20개. 완성 캐릭터가 아니며 머리 결합과 애니메이션 연결이 필요하다.
- 기존 Quaternius RPG: 캐릭터 6종. 현재 전투에서 사용하는 모델이다.
- 모든 팩의 무료 배포 파일을 공식 사이트에서 받았으며 원본 CC0 라이선스를 함께 보관한다.
- glTF/GLB와 참조 텍스처·버퍼를 보관한다. 같은 모델의 FBX/OBJ 중복은 제외한다.
- 팩 원본의 텍스처 변형을 포함하므로 추가 리소스는 약 289 MiB다.

## 코드 연결

```kotlin
val entries = assets.characterAssets.entries
val model = assets.characterAssets.createInstance("kenney-blocky:character-a")
```

목록 조회만으로 모델을 로딩하지 않는다. createInstance는 모델을 지연 로딩하고 독립 ModelInstance를 반환한다.
모델·텍스처 수명은 GameAssets가 관리하므로 호출자가 공유 model을 dispose하지 않는다.
애니메이션 목록은 catalog에 기록하며, 새 모델의 전투 동작 매핑과 장비 부착은 별도 작업이다.

## 전체 목록

| ID | 종류 | 모델 | 애니메이션 수 |
| --- | --- | --- | ---: |
| `kaykit-adventurers:rig_medium_general` | animation | Rig_Medium_General | 15 |
| `kaykit-adventurers:rig_medium_movementbasic` | animation | Rig_Medium_MovementBasic | 11 |
| `kaykit-adventurers:arrow_bow` | equipment | arrow_bow | 0 |
| `kaykit-adventurers:arrow_bow_bundle` | equipment | arrow_bow_bundle | 0 |
| `kaykit-adventurers:arrow_crossbow` | equipment | arrow_crossbow | 0 |
| `kaykit-adventurers:arrow_crossbow_bundle` | equipment | arrow_crossbow_bundle | 0 |
| `kaykit-adventurers:axe_1handed` | equipment | axe_1handed | 0 |
| `kaykit-adventurers:axe_2handed` | equipment | axe_2handed | 0 |
| `kaykit-adventurers:bow` | equipment | bow | 0 |
| `kaykit-adventurers:bow_withstring` | equipment | bow_withString | 0 |
| `kaykit-adventurers:crossbow_1handed` | equipment | crossbow_1handed | 0 |
| `kaykit-adventurers:crossbow_2handed` | equipment | crossbow_2handed | 0 |
| `kaykit-adventurers:dagger` | equipment | dagger | 0 |
| `kaykit-adventurers:mug_empty` | equipment | mug_empty | 0 |
| `kaykit-adventurers:mug_full` | equipment | mug_full | 0 |
| `kaykit-adventurers:quiver` | equipment | quiver | 0 |
| `kaykit-adventurers:shield_badge` | equipment | shield_badge | 0 |
| `kaykit-adventurers:shield_badge_color` | equipment | shield_badge_color | 0 |
| `kaykit-adventurers:shield_round` | equipment | shield_round | 0 |
| `kaykit-adventurers:shield_round_barbarian` | equipment | shield_round_barbarian | 0 |
| `kaykit-adventurers:shield_round_color` | equipment | shield_round_color | 0 |
| `kaykit-adventurers:shield_spikes` | equipment | shield_spikes | 0 |
| `kaykit-adventurers:shield_spikes_color` | equipment | shield_spikes_color | 0 |
| `kaykit-adventurers:shield_square` | equipment | shield_square | 0 |
| `kaykit-adventurers:shield_square_color` | equipment | shield_square_color | 0 |
| `kaykit-adventurers:smokebomb` | equipment | smokebomb | 0 |
| `kaykit-adventurers:spellbook_closed` | equipment | spellbook_closed | 0 |
| `kaykit-adventurers:spellbook_open` | equipment | spellbook_open | 0 |
| `kaykit-adventurers:staff` | equipment | staff | 0 |
| `kaykit-adventurers:sword_1handed` | equipment | sword_1handed | 0 |
| `kaykit-adventurers:sword_2handed` | equipment | sword_2handed | 0 |
| `kaykit-adventurers:sword_2handed_color` | equipment | sword_2handed_color | 0 |
| `kaykit-adventurers:wand` | equipment | wand | 0 |
| `kaykit-adventurers:barbarian` | character | Barbarian | 0 |
| `kaykit-adventurers:knight` | character | Knight | 0 |
| `kaykit-adventurers:mage` | character | Mage | 0 |
| `kaykit-adventurers:ranger` | character | Ranger | 0 |
| `kaykit-adventurers:rogue` | character | Rogue | 0 |
| `kaykit-adventurers:rogue_hooded` | character | Rogue_Hooded | 0 |
| `modular-fantasy:female_peasant_arms` | part | Female_Peasant_Arms | 0 |
| `modular-fantasy:female_peasant_body` | part | Female_Peasant_Body | 1 |
| `modular-fantasy:female_peasant_feet` | part | Female_Peasant_Feet | 0 |
| `modular-fantasy:female_peasant_legs` | part | Female_Peasant_Legs | 0 |
| `modular-fantasy:female_ranger_acc_pauldrons` | part | Female_Ranger_Acc_Pauldrons | 0 |
| `modular-fantasy:female_ranger_arms` | part | Female_Ranger_Arms | 0 |
| `modular-fantasy:female_ranger_body` | part | Female_Ranger_Body | 0 |
| `modular-fantasy:female_ranger_feet` | part | Female_Ranger_Feet | 0 |
| `modular-fantasy:female_ranger_head_hood` | part | Female_Ranger_Head_Hood | 0 |
| `modular-fantasy:female_ranger_legs` | part | Female_Ranger_Legs | 0 |
| `modular-fantasy:male_peasant_arms` | part | Male_Peasant_Arms | 0 |
| `modular-fantasy:male_peasant_body` | part | Male_Peasant_Body | 0 |
| `modular-fantasy:male_peasant_feet` | part | Male_Peasant_Feet | 0 |
| `modular-fantasy:male_peasant_legs` | part | Male_Peasant_Legs | 0 |
| `modular-fantasy:male_ranger_acc_pauldron` | part | Male_Ranger_Acc_Pauldron | 0 |
| `modular-fantasy:male_ranger_arms` | part | Male_Ranger_Arms | 0 |
| `modular-fantasy:male_ranger_body` | part | Male_Ranger_Body | 0 |
| `modular-fantasy:male_ranger_feet_boots` | part | Male_Ranger_Feet_Boots | 0 |
| `modular-fantasy:male_ranger_head_hood` | part | Male_Ranger_Head_Hood | 0 |
| `modular-fantasy:male_ranger_legs` | part | Male_Ranger_Legs | 0 |
| `modular-fantasy:female_peasant` | outfit | Female_Peasant | 0 |
| `modular-fantasy:female_ranger` | outfit | Female_Ranger | 0 |
| `modular-fantasy:male_peasant` | outfit | Male_Peasant | 0 |
| `modular-fantasy:male_ranger` | outfit | Male_Ranger | 0 |
| `kenney-blocky:character-a` | character | character-a | 27 |
| `kenney-blocky:character-b` | character | character-b | 27 |
| `kenney-blocky:character-c` | character | character-c | 27 |
| `kenney-blocky:character-d` | character | character-d | 27 |
| `kenney-blocky:character-e` | character | character-e | 27 |
| `kenney-blocky:character-f` | character | character-f | 27 |
| `kenney-blocky:character-g` | character | character-g | 27 |
| `kenney-blocky:character-h` | character | character-h | 27 |
| `kenney-blocky:character-i` | character | character-i | 27 |
| `kenney-blocky:character-j` | character | character-j | 27 |
| `kenney-blocky:character-k` | character | character-k | 27 |
| `kenney-blocky:character-l` | character | character-l | 27 |
| `kenney-blocky:character-m` | character | character-m | 27 |
| `kenney-blocky:character-n` | character | character-n | 27 |
| `kenney-blocky:character-o` | character | character-o | 27 |
| `kenney-blocky:character-p` | character | character-p | 27 |
| `kenney-blocky:character-q` | character | character-q | 27 |
| `kenney-blocky:character-r` | character | character-r | 27 |
| `rpg-characters:cleric` | character | Cleric | 11 |
| `rpg-characters:monk` | character | Monk | 11 |
| `rpg-characters:ranger` | character | Ranger | 14 |
| `rpg-characters:rogue` | character | Rogue | 12 |
| `rpg-characters:warrior` | character | Warrior | 13 |
| `rpg-characters:wizard` | character | Wizard | 15 |

## 검증

87개 원본 체크섬·텍스처/버퍼 참조 검사, 전체 GLTF/GLB 로딩·해제 검사를 통과했다.
