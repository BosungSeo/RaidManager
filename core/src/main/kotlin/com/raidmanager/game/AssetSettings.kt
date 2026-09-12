package com.raidmanager.game

/** 리소스 경로와 원본 이미지에서 측정한 프레임 배치 설정. */
internal object AssetSettings {
    /** 미리 불러올 전투 효과음 파일 이름. */
    val BATTLE_SOUND_NAMES = listOf("hit", "shot", "skill", "heal", "shield", "interrupt", "wave", "start", "victory", "defeat")
    /** 프레임 애니메이션 시트를 가진 캐릭터와 몬스터 식별자. */
    val ANIMATED_IDS = listOf("aegis", "luna", "rook", "ember", "nyx", "mira", "colossus", "swarm", "slime")
    /** 영웅 시트의 위에서 아래 순서. */
    val HERO_IDS = listOf("aegis", "luna", "rook", "ember", "nyx", "mira")
    /** 몬스터 시트와 전장 배경의 식별자 순서. */
    val MONSTER_IDS = listOf("colossus", "swarm", "slime")
    /** 타이틀 이미지 경로. */
    const val TITLE_PATH = "Title.png"
    /** 영웅 자세 시트 경로. */
    const val HERO_SHEET_PATH = "sprites/raid-heroes.png"
    /** 몬스터 자세 시트 경로. */
    const val MONSTER_SHEET_PATH = "sprites/raid-monsters.png"
    /** 몬스터 효과 시트 경로. */
    const val MONSTER_EFFECT_PATH = "sprites/monster-vfx.png"
    /** 영웅 효과 시트 경로. */
    const val HERO_EFFECT_PATH = "sprites/hero-vfx.png"
    /** 영웅별 잘라낼 위쪽 픽셀 좌표. */
    val HERO_TOPS = intArrayOf(20, 280, 550, 825, 1150, 1440)
    /** 영웅별 잘라낼 아래쪽 픽셀 좌표. */
    val HERO_BOTTOMS = intArrayOf(270, 545, 820, 1135, 1430, 1740)
    /** 영웅별 발 기준점의 원본 세로 좌표. */
    val HERO_FEET = intArrayOf(253, 535, 809, 1115, 1422, 1725)
    /** 영웅 자세별 발 기준점의 원본 가로 좌표. */
    val HERO_ANCHORS = intArrayOf(145, 427, 756)
    /** 몬스터별 잘라낼 위쪽 픽셀 좌표. */
    val MONSTER_TOPS = intArrayOf(0, 480, 870)
    /** 몬스터별 잘라낼 아래쪽 픽셀 좌표. */
    val MONSTER_BOTTOMS = intArrayOf(480, 870, 1254)
    /** 몬스터별 발 기준점의 원본 세로 좌표. */
    val MONSTER_FEET = intArrayOf(440, 830, 1173)
    /** 영웅별 두 번째 자세의 오른쪽 경계. */
    val HERO_MIDDLE_ENDS = intArrayOf(630, 630, 665, 630, 630, 650)
    /** 영웅 첫 번째 자세의 오른쪽 경계. */
    const val HERO_EDGES_START = 285
    /** 영웅 자세 시트의 가로 픽셀 수. */
    const val HERO_SHEET_WIDTH = 887
    /** 몬스터별 첫 번째 자세의 오른쪽 경계. */
    val MONSTER_FIRST_ENDS = intArrayOf(400, 400, 407)
    /** 몬스터별 두 번째 자세의 가로 발 기준점. */
    val MONSTER_MIDDLE_ANCHORS = intArrayOf(690, 650, 705)
    /** 몬스터 두 번째 자세의 오른쪽 경계. */
    const val MONSTER_EDGES_END = 880
    /** 몬스터 자세 시트의 가로 픽셀 수. */
    const val MONSTER_SHEET_WIDTH = 1254
    /** 몬스터 첫 번째 자세의 가로 발 기준점. */
    const val MONSTER_FIRST_ANCHOR = 210
    /** 몬스터 세 번째 자세의 가로 발 기준점. */
    const val MONSTER_LAST_ANCHOR = 1080
    /** 애니메이션 시트의 가로 프레임 수. */
    const val ANIMATION_COLUMNS = 4
    /** 애니메이션 시트의 세로 프레임 수. */
    const val ANIMATION_ROWS = 4
    /** 애니메이션 시트의 전체 프레임 수. */
    const val ANIMATION_FRAME_COUNT = 16
    /** 프레임 너비에 대한 발 가로 기준 비율. */
    const val FOOT_X_RATIO = 0.5f
    /** 프레임 높이에 대한 발 세로 기준 비율. */
    const val FOOT_Y_RATIO = 0.06f
    /** 몬스터 효과 시트의 열 수. */
    const val MONSTER_EFFECT_COLUMNS = 2
    /** 몬스터 효과 시트의 행 수. */
    const val MONSTER_EFFECT_ROWS = 2
    /** 몬스터 효과 이미지 수. */
    const val MONSTER_EFFECT_COUNT = 4
    /** 영웅 효과 시트의 열 수. */
    const val HERO_EFFECT_COLUMNS = 3
    /** 영웅 효과 시트의 행 수. */
    const val HERO_EFFECT_ROWS = 2
    /** 원 텍스처 한 변의 픽셀 수. */
    const val CIRCLE_SIZE = 64
    /** 원 텍스처 중심 좌표. */
    const val CIRCLE_CENTER = 32
    /** 원 텍스처의 반지름. */
    const val CIRCLE_RADIUS = 30
}
