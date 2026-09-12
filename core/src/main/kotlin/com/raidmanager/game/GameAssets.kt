package com.raidmanager.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable

/** Scene에서 함께 사용하는 그래픽 리소스를 생성하고 해제한다. */
class GameAssets : Disposable {
    private val battleSounds = AssetSettings.BATTLE_SOUND_NAMES
        .associateWith { name -> com.badlogic.gdx.Gdx.audio.newSound(com.badlogic.gdx.Gdx.files.internal("audio/$name.wav")) }

    fun playBattleSound(name: String, volume: Float) {
        battleSounds[name]?.play(volume)
    }

    fun stopBattleSounds() = battleSounds.values.forEach { it.stop() }

    val titleImage = Texture(AssetSettings.TITLE_PATH)
    val font = BitmapFont()
    val textLayout = GlyphLayout()
    val buttonTexture: Texture
    val circleTexture: Texture
    private val heroSheet = Texture(AssetSettings.HERO_SHEET_PATH)
    private val monsterSheet = Texture(AssetSettings.MONSTER_SHEET_PATH)
    private val effectSheet = Texture(AssetSettings.MONSTER_EFFECT_PATH).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    val monsterEffects = List(AssetSettings.MONSTER_EFFECT_COUNT) { index ->
        TextureRegion(effectSheet, index % AssetSettings.MONSTER_EFFECT_COLUMNS * (effectSheet.width / AssetSettings.MONSTER_EFFECT_COLUMNS), index / AssetSettings.MONSTER_EFFECT_COLUMNS * (effectSheet.height / AssetSettings.MONSTER_EFFECT_ROWS),
            effectSheet.width / AssetSettings.MONSTER_EFFECT_COLUMNS, effectSheet.height / AssetSettings.MONSTER_EFFECT_ROWS)
    }
    private val heroEffectSheet = Texture(AssetSettings.HERO_EFFECT_PATH).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val heroEffects = com.raidmanager.game.model.SkillType.entries.associateWith { skill ->
        val width = heroEffectSheet.width / AssetSettings.HERO_EFFECT_COLUMNS
        val height = heroEffectSheet.height / AssetSettings.HERO_EFFECT_ROWS
        TextureRegion(heroEffectSheet, skill.ordinal % AssetSettings.HERO_EFFECT_COLUMNS * width, skill.ordinal / AssetSettings.HERO_EFFECT_COLUMNS * height, width, height)
    }

    fun heroEffect(skill: com.raidmanager.game.model.SkillType): TextureRegion = heroEffects.getValue(skill)
    private val battleBackgrounds = AssetSettings.MONSTER_IDS.associateWith { id ->
        Texture("backgrounds/$id.png").apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }
    private val heroRows = AssetSettings.HERO_IDS
    data class SpriteFrame(val region: TextureRegion, val footX: Float, val footY: Float)
    enum class SpritePose { IDLE, ACTION, HURT }

    private val animatedSheets = AssetSettings.ANIMATED_IDS
        .associateWith { id -> Texture("sprites/animated/$id.png").apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        } }
    private val animatedFrames = animatedSheets.mapValues { (_, sheet) ->
        List(AssetSettings.ANIMATION_FRAME_COUNT) { index ->
            val left = index % AssetSettings.ANIMATION_COLUMNS * sheet.width / AssetSettings.ANIMATION_COLUMNS
            val top = index / AssetSettings.ANIMATION_COLUMNS * sheet.height / AssetSettings.ANIMATION_ROWS
            val width = (index % AssetSettings.ANIMATION_COLUMNS + 1) * sheet.width / AssetSettings.ANIMATION_COLUMNS - left
            val height = (index / AssetSettings.ANIMATION_COLUMNS + 1) * sheet.height / AssetSettings.ANIMATION_ROWS - top
            SpriteFrame(TextureRegion(sheet, left, top, width, height), width * AssetSettings.FOOT_X_RATIO, height * AssetSettings.FOOT_Y_RATIO)
        }
    }

    fun animatedFrame(id: String, index: Int): SpriteFrame = animatedFrames.getValue(id)[index.coerceIn(0, AssetSettings.ANIMATION_FRAME_COUNT - 1)]

    // 불균일한 원본 시트는 측정 경계로 자르고 발 좌표를 각 영역의 로컬 좌표로 변환한다.
    private val heroFrames = heroRows.mapIndexed { row, id ->
        val tops = AssetSettings.HERO_TOPS
        val bottoms = AssetSettings.HERO_BOTTOMS
        val feet = AssetSettings.HERO_FEET
        val middleEnd = AssetSettings.HERO_MIDDLE_ENDS[row]
        val edges = intArrayOf(0, AssetSettings.HERO_EDGES_START, middleEnd, AssetSettings.HERO_SHEET_WIDTH)
        val anchors = AssetSettings.HERO_ANCHORS
        id to List(SpritePose.entries.size) { column ->
            SpriteFrame(TextureRegion(heroSheet, edges[column], tops[row],
                edges[column + 1] - edges[column], bottoms[row] - tops[row]),
                (anchors[column] - edges[column]).toFloat(), (bottoms[row] - feet[row]).toFloat())
        }
    }.toMap()

    // 몬스터별 비균일 영역을 자른 후 발 기준점을 보존해 자세 전환 시 위치가 흔들리지 않게 한다.
    private val monsterFrames = AssetSettings.MONSTER_IDS.mapIndexed { row, id ->
        val tops = AssetSettings.MONSTER_TOPS
        val bottoms = AssetSettings.MONSTER_BOTTOMS
        val feet = AssetSettings.MONSTER_FEET
        val edges = intArrayOf(0, AssetSettings.MONSTER_FIRST_ENDS[row], AssetSettings.MONSTER_EDGES_END, AssetSettings.MONSTER_SHEET_WIDTH)
        val anchors = intArrayOf(AssetSettings.MONSTER_FIRST_ANCHOR, AssetSettings.MONSTER_MIDDLE_ANCHORS[row], AssetSettings.MONSTER_LAST_ANCHOR)
        id to List(SpritePose.entries.size) { column ->
            SpriteFrame(TextureRegion(monsterSheet, edges[column], tops[row],
                edges[column + 1] - edges[column], bottoms[row] - tops[row]),
                (anchors[column] - edges[column]).toFloat(), (bottoms[row] - feet[row]).toFloat())
        }
    }.toMap()

    val heroShader: ShaderProgram = createChromaShader()
    val monsterShader: ShaderProgram = createChromaShader(magenta = true)

    fun heroFrame(id: String, pose: SpritePose): SpriteFrame =
        requireNotNull(heroFrames[id]) { "No sprite registered for character: $id" }[pose.ordinal]

    fun monsterFrame(id: String, pose: SpritePose): SpriteFrame =
        requireNotNull(monsterFrames[id]) { "No sprite registered for dungeon: $id" }[pose.ordinal]

    fun battleBackground(dungeonId: String): Texture =
        requireNotNull(battleBackgrounds[dungeonId]) { "No background registered for dungeon: $dungeonId" }

    /** 배경색 우세도를 smoothstep으로 정규화해 투명도를 구하고 가장자리의 배경색 번짐을 제거한다. */
    private fun createChromaShader(magenta: Boolean = false): ShaderProgram {
        val standard = SpriteBatch.createDefaultShader()
        val vertex = standard.vertexShaderSource
        standard.dispose()
        val fragment = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            void main() {
                vec4 texel = texture2D(u_texture, v_texCoords);
                ${if (magenta) "texel.rgb = vec3(1.0) - texel.rgb;" else ""}
                float dominance = texel.g - max(texel.r, texel.b);
                float key = smoothstep(0.15, 0.65, dominance) * smoothstep(0.45, 0.85, texel.g);
                ${if (magenta) "key *= smoothstep(0.45, 0.90, 1.0 - max(texel.r, texel.b));" else ""}
                texel.a *= 1.0 - key;
                if (texel.a < 0.02) discard;
                float spill = smoothstep(0.04, 0.25, dominance);
                texel.g = mix(texel.g, min(texel.g, max(texel.r, texel.b)), spill);
                ${if (magenta) "texel.rgb = vec3(1.0) - texel.rgb;" else ""}
                gl_FragColor = v_color * texel;
            }
        """.trimIndent()
        return ShaderProgram(vertex, fragment).also { check(it.isCompiled) { it.log } }
    }

    init {
        heroSheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        monsterSheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        buttonTexture = Texture(pixmap)
        pixmap.dispose()
        val circle = Pixmap(AssetSettings.CIRCLE_SIZE, AssetSettings.CIRCLE_SIZE, Pixmap.Format.RGBA8888)
        circle.setColor(Color.WHITE)
        circle.fillCircle(AssetSettings.CIRCLE_CENTER, AssetSettings.CIRCLE_CENTER, AssetSettings.CIRCLE_RADIUS)
        circleTexture = Texture(circle)
        circleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        circle.dispose()
    }

    override fun dispose() {
        animatedSheets.values.forEach { it.dispose() }
        battleSounds.values.forEach { it.dispose() }
        titleImage.dispose()
        buttonTexture.dispose()
        circleTexture.dispose()
        heroSheet.dispose()
        heroShader.dispose()
        monsterSheet.dispose()
        effectSheet.dispose()
        heroEffectSheet.dispose()
        monsterShader.dispose()
        battleBackgrounds.values.forEach { it.dispose() }
        font.dispose()
    }
}
