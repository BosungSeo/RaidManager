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
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.BufferUtils
import com.badlogic.gdx.utils.Disposable
import com.raidmanager.game.GameAssets

/** 실제 3D 메시를 투명 타깃에 그려 기존 2D 전장의 정렬·발 기준 변환에 합성한다. GameAssets가 소유한다. */
internal class TankModelRenderer : Disposable {
    private val model = TankModel().build()
    private val instance = ModelInstance(model)
    private val modelBatch = ModelBatch()
    private val target = FrameBuffer(Pixmap.Format.RGBA8888, 384, 384, true)
    private val region = TextureRegion(target.colorBufferTexture).apply {
        flip(false, true)
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val viewport = BufferUtils.newIntBuffer(4)
    private val bladeFacing = Quaternion(Vector3.Y, -90f)
    private val camera = OrthographicCamera(4.4f, 4.4f).apply {
        position.set(0f, 1.7f, 6f)
        near = 0.1f
        far = 20f
        update()
    }
    private val environment = Environment().apply {
        set(ColorAttribute(ColorAttribute.AmbientLight, 0.34f, 0.38f, 0.44f, 1f))
        add(DirectionalLight().set(0.90f, 0.84f, 0.75f, -0.5f, -0.8f, -0.7f))
        add(DirectionalLight().set(0.28f, 0.43f, 0.65f, 0.8f, -0.2f, 0.5f))
    }
    // The 4.4-unit camera has its ground 0.5 units above the bottom edge; retain that exact foot anchor.
    private val frame = GameAssets.SpriteFrame(region, 192f, 384f * 0.5f / 4.4f)
    private val joints = listOf(
        "hips", "torso", "head", "swordHip", "shieldHip", "swordKnee", "shieldKnee",
        "swordShoulder", "swordElbow", "shieldShoulder", "shieldElbow", "shield",
        "cape", "capeMid", "capeEnd", "tabard", "sword",
    ).associateWith { instance.getNode(it) }

    /** Same sixteen indices as aegis.png; frame selection and its priorities remain in SpriteTimeline. */
    private data class Pose(
        val lean: Float = 0f,
        val swordHip: Float = 12f, val shieldHip: Float = -12f,
        val swordKnee: Float = 10f, val shieldKnee: Float = 10f,
        val swordArm: Float = 8f, val swordElbow: Float = -68f,
        val shieldArm: Float = -18f, val shieldElbow: Float = -55f,
        val cape: Float = 12f,
    )

    private val poses = arrayOf(
        Pose(), Pose(swordArm = 6f, cape = 15f), Pose(swordArm = 9f, cape = 19f), Pose(cape = 15f),
        Pose(lean = 12f, swordHip = 42f, shieldHip = -28f, swordKnee = 78f, shieldKnee = 12f, cape = 42f),
        Pose(lean = 14f, swordHip = 18f, shieldHip = -12f, swordKnee = 90f, shieldKnee = 22f, cape = 48f),
        Pose(lean = 12f, swordHip = -28f, shieldHip = 42f, swordKnee = 12f, shieldKnee = 78f, cape = 45f),
        Pose(lean = 14f, swordHip = -12f, shieldHip = 18f, swordKnee = 22f, shieldKnee = 90f, cape = 39f),
        Pose(lean = -7f, swordArm = 105f, swordElbow = -230f, cape = 20f),
        Pose(lean = 18f, swordHip = 28f, shieldHip = -25f, swordArm = -98f, swordElbow = -8f, cape = 34f),
        Pose(lean = 16f, swordHip = 28f, shieldHip = -25f, swordArm = -94f, swordElbow = -12f, cape = 39f),
        Pose(lean = 3f, swordArm = -8f, swordElbow = -40f, cape = 23f),
        Pose(lean = -15f, swordArm = 20f, shieldArm = -40f, shieldElbow = -70f, cape = 8f),
        Pose(lean = 22f, swordKnee = 28f, shieldKnee = 30f, swordArm = 15f, shieldArm = -36f, cape = 26f),
        Pose(lean = 8f, swordKnee = 18f, shieldKnee = 15f, cape = 19f),
        Pose(),
    )

    fun renderFrame(batch: SpriteBatch, frameIndex: Int): GameAssets.SpriteFrame {
        pose(frameIndex)
        // Flush before changing framebuffer, so previously queued actors keep their painter's-order placement.
        batch.end()
        viewport.clear()
        Gdx.gl.glGetIntegerv(GL20.GL_VIEWPORT, viewport)
        target.begin()
        try {
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
            Gdx.gl.glDepthMask(true)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
            modelBatch.begin(camera)
            modelBatch.render(instance, environment)
            modelBatch.end()
        } finally {
            target.end(viewport.get(0), viewport.get(1), viewport.get(2), viewport.get(3))
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)
            Gdx.gl.glDisable(GL20.GL_CULL_FACE)
            batch.begin()
        }
        return frame
    }

    private fun pose(index: Int) {
        val p = poses[index.coerceIn(0, poses.lastIndex)]
        fun rotate(name: String, angle: Float) = joints.getValue(name).rotation.set(Vector3.X, angle)
        rotate("torso", p.lean)
        rotate("head", -p.lean * 0.5f)
        rotate("swordHip", p.swordHip)
        rotate("shieldHip", p.shieldHip)
        rotate("swordKnee", p.swordKnee)
        rotate("shieldKnee", p.shieldKnee)
        rotate("swordShoulder", p.swordArm)
        rotate("swordElbow", p.swordElbow)
        joints.getValue("sword").rotation.set(Vector3.X, if (index == 8) 180f else 0f).mul(bladeFacing)
        rotate("shieldShoulder", p.shieldArm)
        rotate("shieldElbow", p.shieldElbow)
        rotate("shield", -p.shieldArm - p.shieldElbow - p.lean)
        rotate("cape", p.cape)
        rotate("capeMid", -p.cape * 0.30f)
        rotate("capeEnd", p.cape * 0.25f)
        rotate("tabard", -p.lean * 0.5f)
        joints.getValue("hips").translation.y = 1.05f
        instance.calculateTransforms()
        val footY = minOf(
            instance.getNode("swordFoot").globalTransform.`val`[Matrix4.M13],
            instance.getNode("shieldFoot").globalTransform.`val`[Matrix4.M13],
        ) - 0.13f
        joints.getValue("hips").translation.y -= footY
        instance.transform.setToRotation(Vector3.Y, 55f)
        instance.calculateTransforms()
    }

    override fun dispose() {
        target.dispose()
        modelBatch.dispose()
        model.dispose()
    }
}
