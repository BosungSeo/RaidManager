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
    val titleImage = Texture("Title.png")
    val font = BitmapFont()
    val textLayout = GlyphLayout()
    val buttonTexture: Texture
    val circleTexture: Texture
    private val heroSheet = Texture("sprites/raid-heroes.png")
    private val monsterSheet = Texture("sprites/raid-monsters.png")
    private val battleBackgrounds = listOf("colossus", "swarm", "slime").associateWith { id ->
        Texture("backgrounds/$id.png").apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }
    private val heroRows = listOf("aegis", "luna", "rook", "ember", "nyx", "mira")
    data class SpriteFrame(val region: TextureRegion, val footX: Float, val footY: Float)
    enum class SpritePose { IDLE, ACTION, HURT }

    // Measured from the authored 887 x 1774 sheet; generated poses do not form an exact uniform grid.
    private val heroFrames = heroRows.mapIndexed { row, id ->
        val tops = intArrayOf(20, 280, 550, 825, 1150, 1440)
        val bottoms = intArrayOf(270, 545, 820, 1135, 1430, 1740)
        val feet = intArrayOf(253, 535, 809, 1115, 1422, 1725)
        val middleEnd = if (row == 2) 665 else if (row == 5) 650 else 630
        val edges = intArrayOf(0, 285, middleEnd, 887)
        val anchors = intArrayOf(145, 427, 756)
        id to List(3) { column ->
            SpriteFrame(TextureRegion(heroSheet, edges[column], tops[row],
                edges[column + 1] - edges[column], bottoms[row] - tops[row]),
                (anchors[column] - edges[column]).toFloat(), (bottoms[row] - feet[row]).toFloat())
        }
    }.toMap()

    // Authored sheet is 1254 square, with non-uniform rows. Anchors keep each creature's base stationary.
    private val monsterFrames = listOf("colossus", "swarm", "slime").mapIndexed { row, id ->
        val tops = intArrayOf(0, 480, 870)
        val bottoms = intArrayOf(480, 870, 1254)
        val feet = intArrayOf(440, 830, 1173)
        val edges = intArrayOf(0, if (row == 2) 407 else 400, 880, 1254)
        val anchors = intArrayOf(210, if (row == 0) 690 else if (row == 1) 650 else 705, 1080)
        id to List(3) { column ->
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
        val circle = Pixmap(64, 64, Pixmap.Format.RGBA8888)
        circle.setColor(Color.WHITE)
        circle.fillCircle(32, 32, 30)
        circleTexture = Texture(circle)
        circleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        circle.dispose()
    }

    override fun dispose() {
        titleImage.dispose()
        buttonTexture.dispose()
        circleTexture.dispose()
        heroSheet.dispose()
        heroShader.dispose()
        monsterSheet.dispose()
        monsterShader.dispose()
        battleBackgrounds.values.forEach { it.dispose() }
        font.dispose()
    }
}
