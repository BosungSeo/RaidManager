package com.raidmanager.game.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.JsonReader
import net.mgsx.gltf.loaders.glb.GLBLoader
import net.mgsx.gltf.loaders.gltf.GLTFLoader
import net.mgsx.gltf.scene3d.scene.SceneAsset

/** 보관용 캐릭터·의상·장비 목록. 실제 사용한 모델만 GPU에 올리며 GameAssets가 수명을 관리한다. */
class CharacterAssetLibrary : Disposable {
    data class Entry(val id: String, val name: String, val pack: String, val kind: String, val path: String, val animations: List<String>)

    val entries: List<Entry> = JsonReader().parse(Gdx.files.internal("models/character-catalog.json"))
        .get("entries").map { row ->
            Entry(row.getString("id"), row.getString("name"), row.getString("pack"), row.getString("kind"),
                row.getString("path"), row.get("animations").map { it.asString() })
        }
    private val byId = entries.associateBy { it.id }
    private val loaded = mutableMapOf<String, SceneAsset>()

    /** 인스턴스별 자세는 독립적이다. 텍스처·메시는 공유하며 라이브러리가 dispose될 때까지 유효하다. */
    fun createInstance(id: String): ModelInstance {
        val entry = requireNotNull(byId[id]) { "Unknown character asset: $id" }
        val asset = loaded.getOrPut(id) {
            val file = Gdx.files.internal(entry.path)
            // The loader retains temporary state: use a fresh loader for each independent asset.
            if (file.extension().equals("glb", ignoreCase = true)) GLBLoader().load(file) else GLTFLoader().load(file)
        }
        return ModelInstance(asset.scene.model)
    }

    override fun dispose() {
        loaded.values.forEach { it.dispose() }
        loaded.clear()
    }
}
