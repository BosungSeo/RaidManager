package com.raidmanager.game.model

object PrototypeContent {
    val characters = listOf(
        CharacterDefinition("aegis", "AEGIS", Role.TANK, 150f, 7f, 1.5f, "FORTIFY", SkillType.GUARD, 6f),
        CharacterDefinition("luna", "LUNA", Role.HEALER, 88f, 4f, 1.8f, "MEND", SkillType.HEAL, 3.8f),
        CharacterDefinition("rook", "ROOK", Role.DPS, 92f, 12f, 1.1f, "EXECUTE", SkillType.STRIKE, 5f),
        CharacterDefinition("ember", "EMBER", Role.DPS, 84f, 9f, 1.25f, "FIRESTORM", SkillType.CLEAVE, 5f),
        CharacterDefinition("nyx", "NYX", Role.SUPPORT, 94f, 6f, 1.4f, "DISRUPT", SkillType.INTERRUPT, 5.5f),
        CharacterDefinition("mira", "MIRA", Role.SUPPORT, 98f, 7f, 1.35f, "SUNDER", SkillType.SUNDER, 4.5f),
    )

    val dungeons = listOf(
        DungeonDefinition(
            id = "colossus",
            name = "IRON COLOSSUS",
            description = "Heavy raid-wide burst. Guard, heal or interrupt it.",
            enemyName = "COLOSSUS",
            enemyMaxHp = 620f,
            enemyAttack = 13f,
            enemyAttackInterval = 2.1f,
            mechanic = DungeonMechanic.BURST,
            mechanicInterval = 7f,
        ),
        DungeonDefinition(
            id = "swarm",
            name = "SWARM NEST",
            description = "Waves overwhelm slow teams. Cleave excels here.",
            enemyName = "BROODMOTHER",
            enemyMaxHp = 680f,
            enemyAttack = 10f,
            enemyAttackInterval = 1.7f,
            mechanic = DungeonMechanic.SWARM,
            mechanicInterval = 5f,
        ),
        DungeonDefinition(
            id = "slime",
            name = "RENEWING SLIME",
            description = "Regenerates often. Sunder reduces its recovery.",
            enemyName = "PRIME SLIME",
            enemyMaxHp = 520f,
            enemyAttack = 11f,
            enemyAttackInterval = 1.9f,
            mechanic = DungeonMechanic.REGEN,
            mechanicInterval = 4f,
        ),
    )
}
