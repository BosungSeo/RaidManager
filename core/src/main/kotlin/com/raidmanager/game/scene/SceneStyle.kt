package com.raidmanager.game.scene

import com.badlogic.gdx.graphics.Color

/** 화면 표현 설정. 전투 밸런스와 편성 제한은 model의 규칙으로 관리한다. */
internal object SceneStyle {
    object DungeonSelect {
        /** 화면 너비 대비 좌우 여백 비율. */
        const val MARGIN_RATIO = 0.07f
        /** 상단에서 제목까지 거리. */
        const val TITLE_TOP = 42f
        /** 제목 글자 배율. */
        const val TITLE_SCALE = 1.35f
        /** 편성 요약 상단 거리. */
        const val SUMMARY_TOP = 76f
        /** 편성 요약 글자 배율. */
        const val SUMMARY_SCALE = 0.72f
        /** 설명 왼쪽 여백. */
        const val DESCRIPTION_LEFT = 16f
        /** 설명 아래 여백. */
        const val DESCRIPTION_BOTTOM = 24f
        /** 설명 글자 배율. */
        const val DESCRIPTION_SCALE = 0.68f
        /** 하단 안내 높이. */
        const val FOOTER_Y = 28f
        /** 하단 안내 글자 배율. */
        const val FOOTER_SCALE = 0.65f
        /** 던전 카드 높이. */
        const val CARD_HEIGHT = 96f
        /** 첫 카드 아래쪽의 상단 거리. */
        const val CARD_TOP = 190f
        /** 던전 카드 세로 간격. */
        const val CARD_STEP = 116f
    }

    object RaidSetup {
        /** 편성 카드 열 수. */
        const val CARD_COLUMNS = 2
        /** 편성 안내 상단 거리. */
        const val SUMMARY_TOP = 74f
        /** 상태창 부제목 상단 거리. */
        const val STATUS_SUBTITLE_TOP = 58f
        /** 상태 보기 버튼 높이. */
        const val STATUS_BUTTON_HEIGHT = 28f
        /** 몬스터 수 조절 버튼 너비. */
        const val MONSTER_BUTTON_WIDTH = 42f
        /** 카드 내용 왼쪽 여백. */
        const val CARD_TEXT_X = 12f
        /** 상태창 배경색. */
        val STATUS_BACKGROUND = Color(0.035f, 0.05f, 0.09f, 0.98f)
        /** 제목 상단 거리. */
        const val TITLE_TOP = 42f
        /** 제목 배율. */
        const val TITLE_SCALE = 1.35f
        /** 카드 높이. */
        const val CARD_HEIGHT = 74f
        /** 안내 글자 배율. */
        const val SUMMARY_SCALE = 0.78f
        /** 몬스터 안내 높이. */
        const val MONSTER_LABEL_Y = 126f
        /** 몬스터 수 가로 여백. */
        const val MONSTER_VALUE_X = 86f
        /** 몬스터 수 높이. */
        const val MONSTER_VALUE_Y = 143f
        /** 몬스터 수 배율. */
        const val MONSTER_VALUE_SCALE = 0.95f
        /** 카드 간격 및 내용 여백. */
        const val CARD_GAP = 12f
        /** 카드 내용 높이. */
        const val CARD_TEXT_Y = 16f
        /** 카드 내용 배율. */
        const val CARD_TEXT_SCALE = 0.63f
        /** 하단 안내 높이. */
        const val FOOTER_Y = 24f
        /** 하단 안내 배율. */
        const val FOOTER_SCALE = 0.65f
        /** 상태창 내용 여백. */
        const val STATUS_PADDING = 22f
        /** 상태창 제목 상단 거리. */
        const val STATUS_TITLE_TOP = 30f
        /** 상태창 제목 배율. */
        const val STATUS_TITLE_SCALE = 1.05f
        /** 진행 버튼 높이. */
        const val BUTTON_HEIGHT = 58f
        /** 상태 부제목 배율. */
        const val STATUS_SUBTITLE_SCALE = 0.72f
        /** 상태 목록 상단 거리. */
        const val STATUS_CONTENT_TOP = 98f
        /** 상태 행 간격. */
        const val STATUS_LINE_HEIGHT = 28f
        /** 상태 목록 배율. */
        const val STATUS_TEXT_SCALE = 0.7f
        /** 닫기 버튼 오른쪽 거리. */
        const val CLOSE_RIGHT = 116f
        /** 닫기 버튼 아래 여백. */
        const val CLOSE_BOTTOM = 18f
        /** 닫기 버튼 너비. */
        const val CLOSE_WIDTH = 94f
        /** 닫기 버튼 높이. */
        const val CLOSE_HEIGHT = 38f
        /** 상태 버튼 오른쪽 거리. */
        const val STATUS_BUTTON_RIGHT = 92f
        /** 상태 버튼 아래 여백. */
        const val STATUS_BUTTON_BOTTOM = 10f
        /** 상태 버튼 너비. */
        const val STATUS_BUTTON_WIDTH = 80f
        /** 상태창 가로 위치 비율. */
        const val MODAL_X_RATIO = 0.16f
        /** 상태창 세로 위치 비율. */
        const val MODAL_Y_RATIO = 0.18f
        /** 상태창 너비 비율. */
        const val MODAL_WIDTH_RATIO = 0.68f
        /** 상태창 높이 비율. */
        const val MODAL_HEIGHT_RATIO = 0.58f
        /** 화면 좌우 여백 비율. */
        const val MARGIN_RATIO = 0.07f
        /** 첫 카드 상단 거리. */
        const val CARD_TOP = 170f
        /** 진행 버튼 너비. */
        const val CONTINUE_WIDTH = 240f
        /** 진행 버튼 높이 위치. */
        const val CONTINUE_Y = 44f
        /** 증가 버튼 가로 거리. */
        const val PLUS_OFFSET = 130f
        /** 몬스터 버튼 높이 위치. */
        const val MONSTER_BUTTON_Y = 132f
        /** 몬스터 버튼 높이. */
        const val MONSTER_BUTTON_HEIGHT = 34f
    }

    object Game {
        /** 화면에 표시할 최근 전투 사건 수. */
        const val EVENT_COUNT = 3
        /** 전투 화면 기준 너비. */
        const val WIDTH = 1280f
        /** 전투 화면 기준 높이. */
        const val HEIGHT = 720f
        /** 전투 로그 가로 위치. */
        const val EVENT_X = 44f
        /** 로그 제목 높이. */
        const val EVENT_TITLE_Y = 150f
        /** 안내 배율. */
        const val LABEL_SCALE = 0.75f
        /** 로그 첫 행 높이. */
        const val EVENT_Y = 122f
        /** 로그 행 간격. */
        const val EVENT_STEP = 22f
        /** 로그 배율. */
        const val EVENT_SCALE = 0.68f
        /** 결과 버튼 가로 위치. */
        const val RESULT_X = 770f
        /** 결과 버튼 세로 위치. */
        const val RESULT_Y = 55f
        /** 결과 버튼 너비. */
        const val RESULT_WIDTH = 440f
        /** 결과 버튼 높이. */
        const val RESULT_HEIGHT = 64f
        /** 자동 전투 안내 가로 위치. */
        const val AUTO_X = 820f
        /** 자동 전투 안내 높이. */
        const val AUTO_Y = 95f
        /** 시전 안내 높이. */
        const val CAST_Y = 125f
        /** 시전 안내 배율. */
        const val CAST_SCALE = 0.7f
    }

    object BattleResult {
        /** 승리 글자색. */
        val VICTORY_COLOR = Color(0.4f, 0.9f, 0.55f, 1f)
        /** 패배 글자색. */
        val DEFEAT_COLOR = Color(1f, 0.42f, 0.35f, 1f)
        /** 통계 제목 색상. */
        val STATS_COLOR = Color(0.55f, 0.78f, 1f, 1f)
        /** 분석 제목 색상. */
        val ANALYSIS_COLOR = Color(1f, 0.78f, 0.38f, 1f)
        /** 좌우 여백 비율. */
        const val MARGIN_RATIO = 0.07f
        /** 제목 상단 거리. */
        const val TITLE_TOP = 44f
        /** 제목 배율. */
        const val TITLE_SCALE = 1.4f
        /** 요약 상단 거리. */
        const val SUMMARY_TOP = 78f
        /** 요약 배율. */
        const val SUMMARY_SCALE = 0.72f
        /** 통계 제목 상단 거리. */
        const val STATS_TOP = 130f
        /** 항목 글자 배율. */
        const val LABEL_SCALE = 0.82f
        /** 첫 캐릭터 행 상단 거리. */
        const val MEMBER_TOP = 172f
        /** 캐릭터 행 간격. */
        const val MEMBER_STEP = 54f
        /** 통계 가로 여백. */
        const val STATS_OFFSET = 120f
        /** 통계 배율. */
        const val STATS_SCALE = 0.66f
        /** 분석 제목 높이. */
        const val ANALYSIS_TITLE_Y = 248f
        /** 분석 본문 높이. */
        const val ANALYSIS_Y = 218f
        /** 분석 본문 배율. */
        const val ANALYSIS_SCALE = 0.7f
        /** 버튼 간격. */
        const val BUTTON_GAP = 14f
        /** 버튼 높이 위치. */
        const val BUTTON_Y = 64f
        /** 버튼 높이. */
        const val BUTTON_HEIGHT = 58f
    }

    object MainMenu {
        /** 타이틀 위 메뉴 버튼 배경색. */
        val BUTTON_COLOR = Color(0.12f, 0.16f, 0.24f, 0.94f)
        /** 메뉴 버튼 너비. */
        const val BUTTON_WIDTH = 280f
        /** 메뉴 버튼 높이. */
        const val BUTTON_HEIGHT = 64f
        /** 안내 배율. */
        const val MESSAGE_SCALE = 0.85f
        /** 안내 세로 거리. */
        const val MESSAGE_OFFSET = 24f
        /** 버튼 글자 세로 보정. */
        const val TEXT_OFFSET = 10f
        /** 메뉴 가로 위치 비율. */
        const val X_RATIO = 0.08f
        /** 메뉴 세로 위치 비율. */
        const val Y_RATIO = 0.53f
        /** 메뉴 버튼 간격. */
        const val BUTTON_GAP = 18f
    }

    object Intro {
        /** 인트로 재생 시간 초. */
        const val DURATION = 3f
        /** 페이드 시작 시간 초. */
        const val FADE_START = 1.6f
        /** 제목 배율. */
        const val TITLE_SCALE = 1.25f
        /** 제목 가로 위치 비율. */
        const val TITLE_X_RATIO = 0.08f
        /** 제목 세로 위치 비율. */
        const val TITLE_Y_RATIO = 0.72f
    }

    object Ui {
        /** 비활성 버튼 색상. */
        val DISABLED_COLOR = Color(0.14f, 0.15f, 0.18f, 0.9f)
        /** 선택 버튼 색상. */
        val SELECTED_COLOR = Color(0.18f, 0.46f, 0.66f, 0.96f)
        /** 일반 버튼 색상. */
        val BUTTON_COLOR = Color(0.13f, 0.18f, 0.27f, 0.96f)
        /** 게이지 배경색. */
        val BAR_BACKGROUND = Color(0.14f, 0.15f, 0.18f, 1f)
        /** 버튼 글자 배율. */
        const val BUTTON_TEXT_SCALE = 0.86f
        /** 버튼 글자 세로 보정. */
        const val BUTTON_TEXT_OFFSET = 8f
    }

}
