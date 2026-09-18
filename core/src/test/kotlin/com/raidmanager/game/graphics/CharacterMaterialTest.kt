package com.raidmanager.game.graphics

import com.badlogic.gdx.utils.JsonReader
import com.raidmanager.game.model.PrototypeContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CharacterMaterialTest {
    @Test
    fun authoredMaterialsSupportDirectSrgbUnlitRendering() {
        assertEquals(PrototypeContent.characters.map { it.id }.toSet(), CharacterModels.definitions.keys)
        CharacterModels.definitions.values.forEach { definition ->
            val source = assertNotNull(javaClass.classLoader.getResourceAsStream(definition.path))
            val model = source.bufferedReader().use { JsonReader().parse(it.readText()) }
            model.get("materials").forEach { material ->
                assertTrue(material.get("extensions")?.has("KHR_materials_unlit") == true, definition.path)
                val pbr = material.get("pbrMetallicRoughness")
                pbr.get("baseColorFactor")?.forEach { assertEquals(1f, it.asFloat(), definition.path) }
                assertTrue(pbr.has("baseColorTexture"), definition.path)
                assertEquals("OPAQUE", material.getString("alphaMode", "OPAQUE"), definition.path)
            }
            model.get("meshes").forEach { mesh ->
                mesh.get("primitives").forEach { primitive ->
                    primitive.get("attributes").forEach { attribute ->
                        assertFalse(attribute.name.startsWith("COLOR_"), definition.path)
                    }
                }
            }
        }
    }
}
