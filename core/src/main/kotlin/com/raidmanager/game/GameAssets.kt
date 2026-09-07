package com.raidmanager.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.utils.Disposable

/** Scene에서 함께 사용하는 그래픽 리소스를 생성하고 해제한다. */
class GameAssets : Disposable {
    val titleImage = Texture("Title.png")
    val font = BitmapFont()
    val textLayout = GlyphLayout()
    val buttonTexture: Texture

    init {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        buttonTexture = Texture(pixmap)
        pixmap.dispose()
    }

    override fun dispose() {
        titleImage.dispose()
        buttonTexture.dispose()
        font.dispose()
    }
}
