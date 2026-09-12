package com.raidmanager.game.scene

/** 게임 운영 규칙과 독립적인 전투 표현 설정. 거리 단위는 가상 화면 픽셀이다. */
internal object ProjectionStyle {
    /** 전장 좌표의 가로 기준점. */
    const val CENTER_X = 640f
    /** 전장 좌표의 세로 기준점. */
    const val CENTER_Y = 400f
    /** 화면에 투영된 전장 중심 높이. */
    const val SCREEN_Y = 340f
    /** 전장 가로 이동의 화면 가로 배율. */
    const val X_SCALE = 0.82f
    /** 전장 세로 이동에 따른 화면 가로 기울기. */
    const val Y_SKEW = 0.6f
    /** 전장 가로 이동에 따른 화면 세로 기울기. */
    const val X_SKEW = 0.24f
    /** 전장 세로 이동의 화면 세로 배율. */
    const val Y_SCALE = 0.44f
}

internal object SpriteAnimationStyle {
    /** 사망 상태에 고정할 프레임 번호. */
    const val DEAD_FRAME = 13
    /** 대기 동작의 시작 프레임. */
    const val IDLE_FRAME = 0
    /** 이동 동작의 시작 프레임. */
    const val RUN_FRAME = 4
    /** 공격 동작의 시작 프레임. */
    const val ACTION_FRAME = 8
    /** 피격 동작의 시작 프레임. */
    const val HURT_FRAME = 12
    /** 한 동작을 구성하는 프레임 수. */
    const val FRAMES_PER_POSE = 4
    /** 피격 애니메이션 표시 시간(초). */
    const val HURT_DURATION = 0.28f
    /** 공격 애니메이션 표시 시간(초). */
    const val ACTION_DURATION = 0.4f
    /** 피격 프레임 한 장의 유지 시간(초). */
    const val HURT_FRAME_TIME = 0.07f
    /** 공격 및 이동 프레임 유지 시간(초). */
    const val ACTION_FRAME_TIME = 0.1f
    /** 대기 프레임 유지 시간(초). */
    const val IDLE_FRAME_TIME = 0.2f
}

internal object BattleSoundStyle {
    /** 전투 시작 효과음 음량. */
    const val START_VOLUME = 0.25f
    /** 승리·패배 효과음 음량. */
    const val RESULT_VOLUME = 0.35f
    /** 일반 공격 효과음 음량. */
    const val BASIC_VOLUME = 0.16f
    /** 기술 효과음 음량. */
    const val SKILL_VOLUME = 0.25f
    /** 한 번의 갱신에서 재생할 서로 다른 효과음 수. */
    const val MAX_SOUNDS_PER_UPDATE = 3
    /** 일반 공격 효과음의 최소 재생 간격(초). */
    const val BASIC_COOLDOWN = 0.12f
    /** 기술 효과음의 최소 재생 간격(초). */
    const val SKILL_COOLDOWN = 0.3f
}

internal object MonsterAnimationStyle {
    /** 비활성 연출의 초기 경과 시간(초). */
    const val INACTIVE_AGE = 10f
    /** 차단 시 경직 유지 시간(초). */
    const val INTERRUPT_DURATION = 0.5f
    /** 사망 자세로 전환하는 시간(초). */
    const val DEATH_DURATION = 0.7f
    /** 사망 시 가로 확장 비율. */
    const val DEATH_WIDTH = 0.12f
    /** 사망 시 세로 축소 비율. */
    const val DEATH_COLLAPSE = 0.65f
    /** 사망 시 최대 회전각(도). */
    const val DEATH_ROTATION = -12f
    /** 사망 시 감소하는 불투명도. */
    const val DEATH_FADE = 0.6f
    /** 공격 왕복 움직임 시간(초). */
    const val ACTION_DURATION = 0.6f
    /** 피격 색상 유지 시간(초). */
    const val HURT_DURATION = 0.22f
    /** 호흡 진동의 각속도. */
    const val BREATH_SPEED = 3f
    /** 회복 연출 유지 시간(초). */
    const val HEAL_DURATION = 0.8f
    /** 군체 공격의 전진 거리. */
    const val SWARM_STRIDE = 54f
    /** 일반 몬스터 공격의 전진 거리. */
    const val NORMAL_STRIDE = 30f
    /** 피격 시 뒤로 밀리는 거리. */
    const val HURT_OFFSET = 9f
    /** 슬라임 가로 호흡 변형 비율. */
    const val SLIME_WIDTH_BREATH = 0.025f
    /** 슬라임 공격 시 가로 변형 비율. */
    const val SLIME_WIDTH_ACTION = 0.12f
    /** 슬라임 세로 호흡 변형 비율. */
    const val SLIME_HEIGHT_BREATH = 0.04f
    /** 일반 몬스터 세로 호흡 변형 비율. */
    const val NORMAL_HEIGHT_BREATH = 0.012f
    /** 슬라임 공격 시 세로 축소 비율. */
    const val SLIME_HEIGHT_ACTION = 0.1f
    /** 일반 몬스터 공격 시 세로 축소 비율. */
    const val NORMAL_HEIGHT_ACTION = 0.025f
    /** 슬라임 호흡 회전각(도). */
    const val SLIME_ROTATION = 1.5f
    /** 일반 몬스터 공격 회전각(도). */
    const val ACTION_ROTATION = 3f
    /** 광역 기술 잔상 유지 시간(초). */
    const val WAVE_DURATION = 0.7f
}

internal object BattleStageStyle {
    /** 공격·기술 효과의 표시 시간(초). */
    const val EFFECT_LIFETIME = 1.15f
    /** 전투 수치 팝업의 표시 시간(초). */
    const val POPUP_LIFETIME = 1.4f
    /** 화면 흔들림의 초당 감쇠량. */
    const val SHAKE_DECAY = 3.5f
    /** 동일 대상의 팝업을 다른 줄에 배치하는 시간 범위(초). */
    const val POPUP_LANE_WINDOW = 0.65f
    /** 동시에 사용하는 팝업 배치 줄 수. */
    const val POPUP_LANES = 4
    /** 팝업 좌우 분산 거리. */
    const val POPUP_X_SPACING = 24f
    /** 대상 발 위치에서 팝업까지의 기본 높이. */
    const val POPUP_BASE_HEIGHT = 85f
    /** 팝업 줄 사이의 높이. */
    const val POPUP_ROW_HEIGHT = 23f
    /** 강한 화면 흔들림을 표시할 피해량 기준. 피해 계산에는 관여하지 않는다. */
    const val SHAKE_DAMAGE_THRESHOLD = 30f
    /** 강한 타격의 화면 흔들림 세기. */
    const val HIT_SHAKE = 0.24f
    /** 마지막 적 처치의 화면 흔들림 세기. */
    const val DEATH_SHAKE = 0.35f
    /** 이동 애니메이션을 시작하는 화면 거리. */
    const val MOVEMENT_THRESHOLD = 2f
    /** 표시 위치가 전투 좌표를 따라가는 보간 속도. */
    const val FOLLOW_SPEED = 18f
    /** 몬스터마다 호흡 시작 시점을 분산하는 간격. */
    const val MONSTER_PHASE_SPACING = 1.7f
    /** 전투 시작 배너 표시 시간(초). */
    const val ENGAGE_DURATION = 1.8f
    /** 승리 입자 표시 시간(초). */
    const val VICTORY_DURATION = 2.5f
    /** 원 테두리를 구성하는 선분 수. */
    const val RING_SEGMENTS = 48
    /** 원거리 투사체 비행 시간(초). */
    const val PROJECTILE_FLIGHT = 0.3f
}
