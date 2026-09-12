package com.raidmanager.game.model

/** 화면 배치와 독립적인 전투 규칙 및 전투 공간 설정. */
object BattleRules {
    /** 전투 고정 갱신 간격(초). */
    const val STEP = 0.1f
    /** 한 프레임에 누적할 최대 시간(초). */
    const val MAX_FRAME_DELTA = 0.1f
    /** 캐릭터가 보유할 수 있는 최대 보호막. */
    const val MAX_SHIELD = 30f
    /** 몬스터 특수 능력 시전 시간(초). */
    const val ABILITY_CAST_TIME = 1.2f
    /** 몬스터 순서별 공격 간격 증가 비율. */
    const val ENEMY_INTERVAL_OFFSET = 0.09f
    /** 첫 몬스터의 최초 공격 대기 비율. */
    const val ENEMY_OPENING_ATTACK = 0.65f
    /** 몬스터 순서별 최초 공격 지연 비율. */
    const val ENEMY_ATTACK_PHASE = 0.32f
    /** 몬스터 순서별 기믹 시작 지연(초). */
    const val ENEMY_MECHANIC_PHASE = 1.65f
    /** 첫 몬스터 전투 공간 시작 X. */
    const val ENEMY_START_X = 900f
    /** 첫 몬스터 전투 공간 시작 Y. */
    const val ENEMY_START_Y = 440f
    /** 몬스터 시작 위치의 가로 간격. */
    const val ENEMY_SPACING_X = 100f
    /** 몬스터 시작 위치의 세로 간격. */
    const val ENEMY_SPACING_Y = 80f
    /** 기본 최초 스킬 대기 비율. */
    const val INITIAL_SKILL_RATIO = 0.55f
    /** 캐릭터 식별자 기반 시작 시점 구간 수. */
    const val PHASE_BUCKETS = 11
    /** 시작 시점 구간을 비율로 바꾸는 제수. */
    const val PHASE_DIVISOR = 10f
    /** 캐릭터 최초 행동 대기 기본 비율. */
    const val OPENING_TIMER_RATIO = 0.45f
    /** 캐릭터별 최초 공격 대기 변동 폭. */
    const val ATTACK_PHASE_RATIO = 0.5f
    /** 캐릭터별 최초 스킬 대기 변동 폭. */
    const val SKILL_PHASE_RATIO = 0.25f
    /** 근접 캐릭터 전투 공간 시작 X. */
    const val MELEE_START_X = 430f
    /** 원거리 캐릭터 전투 공간 시작 X. */
    const val RANGED_START_X = 300f
    /** 첫 캐릭터 전투 공간 시작 Y. */
    const val MEMBER_START_Y = 570f
    /** 캐릭터 시작 위치의 세로 간격. */
    const val MEMBER_SPACING_Y = 180f
    /** 위협 교환 사용 간격(초). */
    const val TAUNT_INTERVAL = 3f
    /** 모든 캐릭터의 초기 위협도. */
    const val INITIAL_THREAT = 10f
    /** 타이머 만료 판정 오차 허용치. */
    const val TIMER_EPSILON = 0.0001f
    /** 몬스터 일반 공격 사거리. */
    const val ENEMY_ATTACK_RANGE = 155f
    /** 방어 스킬의 파티원별 보호막 부여량. */
    const val GUARD_SHIELD = 13f
    /** 회복 스킬의 최대 회복량. */
    const val HEAL_AMOUNT = 30f
    /** 실제 회복량에 곱하는 위협 배율. */
    const val HEAL_THREAT_MULTIPLIER = 3f
    /** 강타 스킬 기본 피해량. */
    const val STRIKE_DAMAGE = 32f
    /** 군집 던전에서 광역 스킬의 적별 피해량. */
    const val CLEAVE_SWARM_DAMAGE = 50f
    /** 일반 던전에서 광역 스킬의 적별 피해량. */
    const val CLEAVE_DAMAGE = 22f
    /** 차단 스킬 피해량. */
    const val INTERRUPT_DAMAGE = 10f
    /** 회복 감소 유지 시간(초). */
    const val SUNDER_DURATION = 6f
    /** 회복 감소 스킬 피해량. */
    const val SUNDER_DAMAGE = 16f
    /** 방향 벡터 및 후방 판정 오차 허용치. */
    const val DIRECTION_EPSILON = 0.001f
    /** 후방 이동 시 몬스터 주위를 도는 반경. */
    const val FLANK_RADIUS = 132f
    /** 한 단계 후방 이동의 최대 회전각(라디안). */
    const val FLANK_ANGLE_STEP = 0.35f
    /** 전투 이동 공간의 최소 X. */
    const val MIN_X = 180f
    /** 전투 이동 공간의 최대 X. */
    const val MAX_X = 1100f
    /** 전투 이동 공간의 최소 Y. */
    const val MIN_Y = 150f
    /** 전투 이동 공간의 최대 Y. */
    const val MAX_Y = 650f
    /** 후방 이동 목표에 도착했다고 보는 거리. */
    const val FLANK_STOP_DISTANCE = 3f
    /** 후퇴를 끝내는 안전 거리. */
    const val RETREAT_END_RANGE = 290f
    /** 원거리 캐릭터가 후퇴를 시작하는 거리. */
    const val RETREAT_START_RANGE = 230f
    /** 접근 시작 판정의 사거리 여유. */
    const val APPROACH_MARGIN = 8f
    /** 접근 종료 목표의 사거리 여유. */
    const val APPROACH_STOP_MARGIN = 20f
    /** 이동 방향 계산 시 사용하는 최소 거리. */
    const val MIN_MOVEMENT_DISTANCE = 1f
    /** 몬스터가 추격을 멈추는 거리. */
    const val ENEMY_STOP_RANGE = 135f
    /** 몬스터 추격 중 최소 X. */
    const val ENEMY_MIN_X = 320f
    /** 캐릭터 충돌 반경. */
    const val MEMBER_RADIUS = 55f
    /** 몬스터 충돌 반경. */
    const val ENEMY_RADIUS = 65f
    /** 겹침 해소 최대 반복 횟수. */
    const val SEPARATION_ITERATIONS = 96
    /** 충돌 해소 후 확보할 추가 간격. */
    const val SEPARATION_PADDING = 0.02f
    /** 두 개체가 각각 부담하는 밀어내기 비율. */
    const val SEPARATION_SHARE = 0.5f
    /** 자리 조정 상태로 표시할 최소 밀림 거리. */
    const val SPACING_INTENT_THRESHOLD = 0.5f
    /** 탱커의 일반 공격 피격 배율. */
    const val TANK_DAMAGE_MULTIPLIER = 0.68f
    /** 폭발 기믹의 파티원별 피해량. */
    const val BURST_DAMAGE = 22f
    /** 군집 기믹의 파티원별 피해량. */
    const val SWARM_DAMAGE = 11f
    /** 회복 감소 중 몬스터 회복량. */
    const val REDUCED_REGEN = 7f
    /** 몬스터 기본 회복량. */
    const val REGEN_AMOUNT = 32f
    /** 보호 담당자와 탱커의 피해 위협 배율. */
    const val PROTECTOR_THREAT_MULTIPLIER = 5f
    /** 피해 과다 분석에 사용하는 최대 생존 인원. */
    const val LOW_SURVIVOR_COUNT = 1
    /** 후방 공격 피해 배율. */
    const val REAR_DAMAGE_MULTIPLIER = 1.5f
    /** 위협 비율을 백분율로 바꾸는 배율. */
    const val PERCENT_SCALE = 100f
    /** 근접 공격 사거리. */
    const val MELEE_RANGE = 145f
    /** 원거리 공격 사거리. */
    const val RANGED_RANGE = 520f
    /** 근접 캐릭터 기본 이동 속도. */
    const val DEFAULT_MELEE_SPEED = 430f
    /** 원거리 캐릭터 기본 이동 속도. */
    const val DEFAULT_RANGED_SPEED = 260f
    /** 기본 캐릭터 능력치. */
    const val DEFAULT_STAT = 5
    /** 기본 전투 제한 시간(초). */
    const val DEFAULT_TIME_LIMIT = 40f
    /** 몬스터 기본 이동 속도. */
    const val DEFAULT_ENEMY_SPEED = 95f
    /** 공격대 편성 인원. */
    const val PARTY_SIZE = 3
    /** 최소 몬스터 수. */
    const val MIN_MONSTER_COUNT = 1
    /** 최대 몬스터 수. */
    const val MAX_MONSTER_COUNT = 3
}
