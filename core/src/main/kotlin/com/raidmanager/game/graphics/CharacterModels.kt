package com.raidmanager.game.graphics

/** 전투 능력치와 분리된 외부 캐릭터 모델·원본 애니메이션 연결 정보. */
internal object CharacterModels {
    data class Definition(val path: String, val clips: ExternalModelRenderer.Clips)

    private fun model(name: String, idle: String, walk: String, attack: String) = Definition(
        "models/rpg-characters/$name.gltf",
        ExternalModelRenderer.Clips(
            idle = idle, walk = walk, attack = attack, hit = "RecieveHit",
            attackDuration = CharacterMotion.ATTACK_DURATION, hitDuration = CharacterMotion.HIT_DURATION,
        ),
    )

    val definitions = mapOf(
        "aegis" to model("Warrior", "Idle_Weapon", "Run_Weapon", "Sword_Attack"),
        "luna" to model("Cleric", "Idle_Weapon", "Run", "Spell1"),
        "rook" to model("Rogue", "Attacking_Idle", "Run", "Dagger_Attack"),
        "ember" to model("Wizard", "Idle_Weapon", "Run_Weapon", "Spell1"),
        "nyx" to model("Ranger", "Idle_Weapon", "Run_Holding", "Bow_Shoot"),
        "mira" to model("Monk", "Idle_Attacking", "Run", "Attack2"),
    )
}
