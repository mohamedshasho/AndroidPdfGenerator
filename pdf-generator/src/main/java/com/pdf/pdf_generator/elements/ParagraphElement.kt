package com.pdf.pdf_generator.elements

import android.graphics.Color
import android.graphics.Typeface
import com.pdf.pdf_generator.Fonts


data class ParagraphElement(
    val text: String,
    val font: Typeface = Fonts.Default.font,
    val size: Float = 11f,
    val align: TextAlign = TextAlign.DEFAULT,
    val color: Int = Color.BLACK,
) : Element() {
    enum class TextAlign {
        DEFAULT, CENTER, OPPOSITE
    }
}