package com.pdf.pdf_generator.elements

import android.graphics.Bitmap

data class ImageElement(
    val bitmap: Bitmap,
    val width: Int = bitmap.width,
    val height: Int = bitmap.height,
    val align: ImageAlign = ImageAlign.CENTER,
    val alpha: Int = 100,
) : Element() {
    enum class ImageAlign { LEFT, CENTER, RIGHT }
}
