package com.raidmanager.game.model

/** 캐릭터와 던전의 원본 밸런스 데이터. 공통 계산 계수는 BattleRules와 StatRules에서 관리한다. */
object PrototypeContent {
    /** AEGIS 캐릭터의 기본 수치 및 능력치. */
    private val aegis = CharacterDefinition(
        id = "aegis", // 저장 및 선택에 사용하는 식별자
        name = "AEGIS", // 표시 이름
        role = Role.TANK, // 전투 역할
        maxHp = 150f, // 기본 최대 체력
        attackPower = 7f, // 기본 일반 공격력
        attackInterval = 1.5f, // 기본 공격 간격(초)
        skillName = "FORTIFY", // 스킬 표시 이름
        skillType = SkillType.GUARD, // 스킬 동작 종류
        skillCooldown = 6f, // 기본 스킬 대기 시간(초)
        moveSpeed = 400f, // 초당 전투 공간 이동 거리
        stats = CharacterStats(
            vitality = 12, // 활력
            strength = 6, // 힘
            intelligence = 3, // 지능
            dexterity = 4, // 민첩
            endurance = 12, // 인내
            wisdom = 5, // 지혜
        ),
    )

    /** LUNA 캐릭터의 기본 수치 및 능력치. */
    private val luna = CharacterDefinition(
        id = "luna", // 저장 및 선택에 사용하는 식별자
        name = "LUNA", // 표시 이름
        role = Role.HEALER, // 전투 역할
        maxHp = 88f, // 기본 최대 체력
        attackPower = 4f, // 기본 일반 공격력
        attackInterval = 1.8f, // 기본 공격 간격(초)
        skillName = "MEND", // 스킬 표시 이름
        skillType = SkillType.HEAL, // 스킬 동작 종류
        skillCooldown = 3.8f, // 기본 스킬 대기 시간(초)
        moveSpeed = 240f, // 초당 전투 공간 이동 거리
        stats = CharacterStats(
            vitality = 5, // 활력
            strength = 2, // 힘
            intelligence = 12, // 지능
            dexterity = 6, // 민첩
            endurance = 4, // 인내
            wisdom = 10, // 지혜
        ),
    )

    /** ROOK 캐릭터의 기본 수치 및 능력치. */
    private val rook = CharacterDefinition(
        id = "rook", // 저장 및 선택에 사용하는 식별자
        name = "ROOK", // 표시 이름
        role = Role.DPS, // 전투 역할
        maxHp = 92f, // 기본 최대 체력
        attackPower = 12f, // 기본 일반 공격력
        attackInterval = 1.1f, // 기본 공격 간격(초)
        skillName = "EXECUTE", // 스킬 표시 이름
        skillType = SkillType.STRIKE, // 스킬 동작 종류
        skillCooldown = 5f, // 기본 스킬 대기 시간(초)
        moveSpeed = 430f, // 초당 전투 공간 이동 거리
        stats = CharacterStats(
            vitality = 5, // 활력
            strength = 12, // 힘
            intelligence = 2, // 지능
            dexterity = 10, // 민첩
            endurance = 4, // 인내
            wisdom = 3, // 지혜
        ),
    )

    /** EMBER 캐릭터의 기본 수치 및 능력치. */
    private val ember = CharacterDefinition(
        id = "ember", // 저장 및 선택에 사용하는 식별자
        name = "EMBER", // 표시 이름
        role = Role.DPS, // 전투 역할
        maxHp = 84f, // 기본 최대 체력
        attackPower = 9f, // 기본 일반 공격력
        attackInterval = 1.25f, // 기본 공격 간격(초)
        skillName = "FIRESTORM", // 스킬 표시 이름
        skillType = SkillType.CLEAVE, // 스킬 동작 종류
        skillCooldown = 5f, // 기본 스킬 대기 시간(초)
        moveSpeed = 250f, // 초당 전투 공간 이동 거리
        stats = CharacterStats(
            vitality = 4, // 활력
            strength = 7, // 힘
            intelligence = 12, // 지능
            dexterity = 6, // 민첩
            endurance = 3, // 인내
            wisdom = 4, // 지혜
        ),
    )

    /** NYX 캐릭터의 기본 수치 및 능력치. */
    private val nyx = CharacterDefinition(
        id = "nyx", // 저장 및 선택에 사용하는 식별자
        name = "NYX", // 표시 이름
        role = Role.SUPPORT, // 전투 역할
        maxHp = 94f, // 기본 최대 체력
        attackPower = 6f, // 기본 일반 공격력
        attackInterval = 1.4f, // 기본 공격 간격(초)
        skillName = "DISRUPT", // 스킬 표시 이름
        skillType = SkillType.INTERRUPT, // 스킬 동작 종류
        skillCooldown = 5.5f, // 기본 스킬 대기 시간(초)
        moveSpeed = 280f, // 초당 전투 공간 이동 거리
        stats = CharacterStats(
            vitality = 5, // 활력
            strength = 4, // 힘
            intelligence = 7, // 지능
            dexterity = 10, // 민첩
            endurance = 5, // 인내
            wisdom = 11, // 지혜
        ),
    )

    /** MIRA 캐릭터의 기본 수치 및 능력치. */
    private val mira = CharacterDefinition(
        id = "mira", // 저장 및 선택에 사용하는 식별자
        name = "MIRA", // 표시 이름
        role = Role.SUPPORT, // 전투 역할
        maxHp = 98f, // 기본 최대 체력
        attackPower = 7f, // 기본 일반 공격력
        attackInterval = 1.35f, // 기본 공격 간격(초)
        skillName = "SUNDER", // 스킬 표시 이름
        skillType = SkillType.SUNDER, // 스킬 동작 종류
        skillCooldown = 4.5f, // 기본 스킬 대기 시간(초)
        moveSpeed = 260f, // 초당 전투 공간 이동 거리
        stats = CharacterStats(
            vitality = 6, // 활력
            strength = 6, // 힘
            intelligence = 8, // 지능
            dexterity = 8, // 민첩
            endurance = 6, // 인내
            wisdom = 9, // 지혜
        ),
    )

    /** 선택 화면에 노출하는 캐릭터 순서. */
    val characters = listOf(aegis, luna, rook, ember, nyx, mira)

    /** 선택 가능한 던전과 몬스터 밸런스 데이터. */
    val dungeons = listOf(
        DungeonDefinition(
            id = "demon",
            name = "DEMON SANCTUM",
            description = "Demon unleashes a crushing burst. Guard or interrupt.",
            enemyName = "DEMON",
            enemyMaxHp = 650f,
            enemyAttack = 14f,
            enemyAttackInterval = 2f,
            mechanic = DungeonMechanic.BURST,
            enemyMoveSpeed = 90f,
            mechanicInterval = 7f,
        ),
        DungeonDefinition(
            id = "colossus",
            name = "IRON COLOSSUS",
            description = "Heavy raid-wide burst. Guard, heal or interrupt it.",
            enemyName = "COLOSSUS",
            enemyMaxHp = 620f, // 몬스터 최대 체력
            enemyAttack = 13f, // 일반 공격 피해량
            enemyAttackInterval = 2.1f, // 일반 공격 간격(초)
            mechanic = DungeonMechanic.BURST,
            enemyMoveSpeed = 85f, // 초당 전투 공간 이동 거리
            mechanicInterval = 7f, // 특수 기믹 반복 간격(초)
        ),
        DungeonDefinition(
            id = "swarm",
            name = "SWARM NEST",
            description = "Waves overwhelm slow teams. Cleave excels here.",
            enemyName = "BROODMOTHER",
            enemyMaxHp = 680f, // 몬스터 최대 체력
            enemyAttack = 10f, // 일반 공격 피해량
            enemyAttackInterval = 1.7f, // 일반 공격 간격(초)
            mechanic = DungeonMechanic.SWARM,
            enemyMoveSpeed = 115f, // 초당 전투 공간 이동 거리
            mechanicInterval = 5f, // 특수 기믹 반복 간격(초)
        ),
        DungeonDefinition(
            id = "slime",
            name = "RENEWING SLIME",
            description = "Regenerates often. Sunder reduces its recovery.",
            enemyName = "PRIME SLIME",
            enemyMaxHp = 520f, // 몬스터 최대 체력
            enemyAttack = 11f, // 일반 공격 피해량
            enemyAttackInterval = 1.9f, // 일반 공격 간격(초)
            mechanic = DungeonMechanic.REGEN,
            enemyMoveSpeed = 70f, // 초당 전투 공간 이동 거리
            mechanicInterval = 4f, // 특수 기믹 반복 간격(초)
        ),
    )
}
