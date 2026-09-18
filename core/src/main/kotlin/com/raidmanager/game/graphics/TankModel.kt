package com.raidmanager.game.graphics

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute
import com.badlogic.gdx.graphics.g3d.model.Node
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3

/** AEGIS의 강철 갑옷·검·방패·청색 망토를 가진 관절형 메시. +Y는 위, +Z는 정면이다. */
internal class TankModel {
    private val builder = ModelBuilder()
    private val children = mutableListOf<Node>()
    private var partIndex = 0
    private val steel = material("steel", "687787", 42f)
    private val darkSteel = material("darkSteel", "293440", 28f)
    private val gold = material("gold", "B58A48", 48f)
    private val blade = material("blade", "D4DFE6", 64f)
    private val cloth = material("cloth", "155785")
    private val clothDark = material("clothShadow", "102F50")
    private val leather = material("leather", "342921")
    private val skin = material("skin", "C8926D")
    private val hair = material("hair", "38251C")

    fun build(): Model {
        builder.begin()
        val root = joint("root", null, 0f, 0f, 0f)
        val hips = joint("hips", root, 0f, 1.05f, 0f)
        box(darkSteel, 0f, 0f, 0f, 0.62f, 0.28f, 0.38f)
        box(leather, 0f, 0.13f, 0f, 0.67f, 0.10f, 0.42f)
        sphere(gold, 0f, 0.13f, 0.24f, 0.13f, 0.13f, 0.06f)

        val torso = joint("torso", hips, 0f, 0.17f, 0f)
        sphere(darkSteel, 0f, 0.32f, 0f, 0.78f, 0.72f, 0.44f)
        sphere(steel, 0f, 0.42f, 0.04f, 0.79f, 0.49f, 0.47f)
        box(cloth, 0f, 0.34f, 0.267f, 0.29f, 0.51f, 0.035f)
        box(gold, 0f, 0.40f, 0.293f, 0.035f, 0.26f, 0.018f)
        box(gold, 0f, 0.43f, 0.296f, 0.17f, 0.035f, 0.018f)
        for (side in listOf(-1f, 1f)) {
            box(gold, side * 0.16f, 0.34f, 0.276f, 0.025f, 0.49f, 0.02f)
            sphere(gold, side * 0.30f, 0.62f, 0.19f, 0.08f, 0.08f, 0.04f)
        }
        for (i in 0..2) sphere(steel, 0f, 0.05f + i * 0.07f, 0f, 0.65f, 0.11f, 0.40f)
        sphere(darkSteel, 0f, 0.67f, 0f, 0.46f, 0.14f, 0.38f)

        joint("head", torso, 0f, 0.77f, 0f)
        sphere(skin, 0f, 0.15f, 0.01f, 0.35f, 0.43f, 0.34f)
        sphere(hair, 0f, 0.31f, -0.035f, 0.39f, 0.23f, 0.37f)
        sphere(hair, 0f, 0.12f, -0.12f, 0.34f, 0.32f, 0.16f)
        sphere(hair, 0f, 0.005f, 0.115f, 0.30f, 0.17f, 0.20f)
        sphere(skin, 0f, 0.13f, 0.19f, 0.08f, 0.12f, 0.12f)
        for (side in listOf(-1f, 1f)) {
            sphere(skin, side * 0.18f, 0.13f, 0f, 0.07f, 0.12f, 0.08f)
            box(hair, side * 0.083f, 0.21f, 0.16f, 0.10f, 0.035f, 0.04f)
            box(leather, side * 0.083f, 0.17f, 0.169f, 0.045f, 0.026f, 0.024f)
        }

        leg(hips, "sword", -0.22f)
        leg(hips, "shield", 0.22f)
        arm(torso, "sword", -0.47f)
        arm(torso, "shield", 0.47f)

        joint("tabard", hips, 0f, 0f, 0.23f)
        box(cloth, 0f, -0.30f, 0f, 0.36f, 0.65f, 0.045f)
        for (side in listOf(-1f, 1f)) box(gold, side * 0.16f, -0.30f, 0.03f, 0.025f, 0.63f, 0.02f)
        box(gold, 0f, -0.36f, 0.03f, 0.025f, 0.23f, 0.02f)

        val cape = joint("cape", torso, 0f, 0.60f, -0.23f)
        capePanel(0.75f, 0.94f, 0.52f)
        val capeMid = joint("capeMid", cape, 0f, -0.52f, -0.11f)
        capePanel(0.94f, 1.10f, 0.52f)
        joint("capeEnd", capeMid, 0f, -0.52f, -0.11f)
        capePanel(1.10f, 1.20f, 0.42f)

        return builder.end().also { model ->
            // ModelBuilder initially registers every node as a root; retain the articulated hierarchy only once.
            children.forEach { model.nodes.removeValue(it, true) }
            model.calculateTransforms()
        }
    }

    private fun leg(parent: Node, name: String, x: Float) {
        val hip = joint("${name}Hip", parent, x, -0.05f, 0f)
        sphere(darkSteel, 0f, -0.18f, 0f, 0.28f, 0.42f, 0.30f)
        box(steel, 0f, -0.17f, 0.12f, 0.29f, 0.33f, 0.09f)
        box(gold, 0f, -0.04f, 0.17f, 0.29f, 0.035f, 0.025f)
        val knee = joint("${name}Knee", hip, 0f, -0.40f, 0f)
        sphere(steel, 0f, -0.01f, 0.075f, 0.29f, 0.24f, 0.31f)
        sphere(darkSteel, 0f, -0.23f, 0f, 0.21f, 0.42f, 0.23f)
        box(steel, 0f, -0.23f, 0.11f, 0.15f, 0.30f, 0.06f)
        joint("${name}Foot", knee, 0f, -0.47f, 0.06f)
        sphere(darkSteel, 0f, -0.035f, 0.055f, 0.27f, 0.19f, 0.43f)
        sphere(steel, 0f, -0.025f, 0.16f, 0.28f, 0.15f, 0.27f)
    }

    private fun arm(parent: Node, name: String, x: Float) {
        val shoulder = joint("${name}Shoulder", parent, x, 0.52f, 0f)
        sphere(darkSteel, 0f, -0.18f, 0f, 0.25f, 0.37f, 0.28f)
        sphere(gold, 0f, -0.015f, 0f, 0.44f, 0.30f, 0.47f)
        sphere(steel, 0f, 0.025f, 0f, 0.42f, 0.32f, 0.45f)
        sphere(steel, 0f, -0.24f, 0.04f, 0.28f, 0.18f, 0.30f)
        val elbow = joint("${name}Elbow", shoulder, 0f, -0.34f, 0f)
        sphere(darkSteel, 0f, 0f, 0f, 0.22f, 0.22f, 0.24f)
        sphere(steel, 0f, -0.18f, 0f, 0.24f, 0.35f, 0.28f)
        box(gold, 0f, -0.29f, 0f, 0.25f, 0.045f, 0.28f)
        sphere(leather, 0f, -0.37f, 0f, 0.19f, 0.20f, 0.21f)
        if (name == "sword") {
            joint("sword", elbow, 0f, -0.38f, 0f).rotation.set(Vector3.Y, -90f)
            box(leather, 0f, -0.04f, 0f, 0.075f, 0.23f, 0.075f)
            sphere(gold, 0f, 0.09f, 0f, 0.12f, 0.12f, 0.12f)
            box(gold, 0f, -0.16f, 0f, 0.36f, 0.055f, 0.10f)
            kite(blade, 0.16f, 0.94f, 0.045f, -0.18f)
        } else {
            // The shield remains upright on the forearm, with its raised boss facing forward.
            joint("shield", elbow, 0f, -0.22f, 0.14f)
            kite(gold, 0.76f, 1.04f, 0.12f, 0.40f)
            kite(darkSteel, 0.65f, 0.91f, 0.10f, 0.35f, 0.055f)
            sphere(gold, 0f, -0.02f, 0.14f, 0.28f, 0.31f, 0.14f)
            sphere(steel, 0f, -0.02f, 0.20f, 0.16f, 0.19f, 0.10f)
            box(gold, 0f, -0.02f, 0.16f, 0.035f, 0.65f, 0.035f)
            for (side in listOf(-1f, 1f)) {
                sphere(gold, side * 0.25f, 0.27f, 0.11f, 0.06f, 0.06f, 0.04f)
            }
        }
    }

    private fun capePanel(top: Float, bottom: Float, height: Float) {
        // Folded, double-sided cloth has actual depth and follows three independently posed joints.
        val mesh = part(cloth)
        for (i in 0..5) {
            val u = i / 6f - 0.5f
            val v = (i + 1) / 6f - 0.5f
            val z0 = if (i % 2 == 0) 0.04f else -0.04f
            val z1 = -z0
            val a = Vector3(u * top, 0f, z0)
            val b = Vector3(v * top, 0f, z1)
            val c = Vector3(v * bottom, -height, z1 - 0.11f)
            val d = Vector3(u * bottom, -height, z0 - 0.11f)
            mesh.triangle(a, b, c)
            mesh.triangle(c, d, a)
            mesh.triangle(c, b, a)
            mesh.triangle(a, d, c)
        }
        box(clothDark, 0f, -0.02f, 0f, top, 0.04f, 0.07f)
    }

    private fun kite(material: Material, width: Float, height: Float, depth: Float, top: Float, z: Float = 0f) {
        val mesh = part(material)
        val outline = listOf(
            Vector3(-width * 0.5f, top, z), Vector3(width * 0.5f, top, z),
            Vector3(width * 0.44f, top - height * 0.58f, z), Vector3(0f, top - height, z),
            Vector3(-width * 0.44f, top - height * 0.58f, z),
        )
        val front = Vector3(0f, top - height * 0.35f, z + depth)
        val back = Vector3(0f, top - height * 0.35f, z - depth * 0.3f)
        outline.indices.forEach { i ->
            val a = outline[i]
            val b = outline[(i + 1) % outline.size]
            mesh.triangle(b, a, front)
            mesh.triangle(a, b, back)
        }
    }

    private fun joint(id: String, parent: Node?, x: Float, y: Float, z: Float): Node = builder.node().also {
        it.id = id
        it.translation.set(x, y, z)
        if (parent != null) {
            parent.addChild(it)
            children += it
        }
    }

    private fun part(material: Material): MeshPartBuilder =
        builder.part("part${partIndex++}", GL20.GL_TRIANGLES, (Usage.Position or Usage.Normal).toLong(), material)

    private fun box(material: Material, x: Float, y: Float, z: Float, w: Float, h: Float, d: Float) {
        BoxShapeBuilder.build(part(material), x, y, z, w, h, d)
    }

    private fun sphere(material: Material, x: Float, y: Float, z: Float, w: Float, h: Float, d: Float) {
        val mesh = part(material)
        mesh.setVertexTransform(Matrix4().setToTranslation(x, y, z))
        SphereShapeBuilder.build(mesh, w, h, d, 12, 8)
        mesh.setVertexTransform(null)
    }

    private fun material(id: String, hex: String, shininess: Float = 0f): Material = Material(id).apply {
        set(ColorAttribute.createDiffuse(Color.valueOf(hex)))
        if (shininess > 0f) {
            set(ColorAttribute.createSpecular(Color(0.6f, 0.65f, 0.7f, 1f)))
            set(FloatAttribute.createShininess(shininess))
        }
    }
}
