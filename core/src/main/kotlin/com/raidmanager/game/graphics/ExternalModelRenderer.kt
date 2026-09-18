package com.raidmanager.game.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.utils.BufferUtils
import com.badlogic.gdx.utils.Disposable
import com.raidmanager.game.GameAssets
import net.mgsx.gltf.loaders.gltf.GLTFLoader
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig

/** 외부 glTF 메시와 원본 골격 애니메이션을 투명 전장 프레임으로 렌더링한다. GameAssets가 소유한다. */
internal class ExternalModelRenderer(
    modelPath: String = "models/demon/Demon.gltf",
    facingDegrees: Float = -55f,
    private val clips: Clips = Clips(),
    frameWidth: Int = 384,
    originalUnlitColors: Boolean = false,
) : Disposable {
    data class Clips(
        val idle: String = "Idle",
        val walk: String = "Walk",
        val attack: String = "Punch",
        val hit: String = "HitReact",
        val death: String = "Death",
        val attackDuration: Float = 0.7f,
        val hitDuration: Float = 0.3f,
    )

    private val asset = GLTFLoader().load(Gdx.files.internal(modelPath))
    private val scene = Scene(asset.scene)
    private val modelBatch = ModelBatch(PBRShaderProvider.createDefault(
        PBRShaderProvider.createDefaultConfig().apply {
            numBones = 64
            if (originalUnlitColors) {
                // These authored unlit textures have white factors and no vertex colors. Copy their sRGB
                // values to the RGBA8888 target; SpriteBatch presents that target without another gamma pass.
                manualSRGB = PBRShaderConfig.SRGB.NONE
                manualGammaCorrection = false
                numDirectionalLights = 0
                numPointLights = 0
                numSpotLights = 0
            }
        },
    ))
    private val target = FrameBuffer(Pixmap.Format.RGBA8888, frameWidth, 384, true)
    private val region = TextureRegion(target.colorBufferTexture).apply {
        flip(false, true)
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val frame = GameAssets.SpriteFrame(region, frameWidth / 2f, 38.4f)
    private val viewport = BufferUtils.newIntBuffer(4)
    private val camera = OrthographicCamera(4.4f * frameWidth / 384f, 4.4f).apply {
        position.set(0f, 1.76f, 8f)
        near = 0.1f
        far = 30f
        update()
    }
    private val environment = Environment().apply {
        if (!originalUnlitColors) {
            set(ColorAttribute(ColorAttribute.AmbientLight, 0.7f, 0.7f, 0.7f, 1f))
            add(DirectionalLight().set(1f, 0.9f, 0.8f, -0.5f, -0.8f, -0.7f))
        }
    }

    init {
        listOf(clips.idle, clips.walk, clips.attack, clips.hit, clips.death).forEach {
            requireNotNull(scene.modelInstance.getAnimation(it)) { "Missing animation $it in $modelPath" }
        }
        val bounds = scene.modelInstance.calculateBoundingBox(BoundingBox())
        val scale = 3.2f / maxOf(bounds.height, bounds.width, bounds.depth)
        val center = bounds.getCenter(Vector3())
        scene.modelInstance.transform.setToRotation(Vector3.Y, facingDegrees)
            .scale(scale, scale, scale).translate(-center.x, -bounds.min.y, -center.z)
    }

    fun renderFrame(
        batch: SpriteBatch, clock: Float, moving: Boolean, actionAge: Float?, hitAge: Float?, alive: Boolean,
    ): GameAssets.SpriteFrame {
        val animation = when {
            !alive -> clips.death
            hitAge != null && hitAge < clips.hitDuration -> clips.hit
            actionAge != null && actionAge < clips.attackDuration -> clips.attack
            moving -> clips.walk
            else -> clips.idle
        }
        val duration = scene.modelInstance.getAnimation(animation).duration
        val time = when (animation) {
            clips.death -> duration
            clips.hit -> (hitAge!! / clips.hitDuration * duration).coerceAtMost(duration)
            clips.attack -> (actionAge!! / clips.attackDuration * duration).coerceAtMost(duration)
            else -> clock % duration
        }
        // Sample absolute time: multiple enemies share the renderer without advancing one another's animation.
        scene.animationController.allowSameAnimation = true
        scene.animationController.setAnimation(animation, 1)
        scene.animationController.update(time)
        batch.end()
        viewport.clear()
        Gdx.gl.glGetIntegerv(GL20.GL_VIEWPORT, viewport)
        target.begin()
        try {
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
            Gdx.gl.glDepthMask(true)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
            modelBatch.begin(camera)
            modelBatch.render(scene, environment)
            modelBatch.end()
        } finally {
            target.end(viewport.get(0), viewport.get(1), viewport.get(2), viewport.get(3))
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)
            Gdx.gl.glDisable(GL20.GL_CULL_FACE)
            batch.begin()
        }
        return frame
    }

    override fun dispose() {
        target.dispose()
        modelBatch.dispose()
        asset.dispose()
    }
}
