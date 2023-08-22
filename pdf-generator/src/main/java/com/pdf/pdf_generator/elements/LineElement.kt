package com.pdf.pdf_generator.elements

import android.graphics.Color


data class LineElement(
    val width: Float = 1f,
    val color: Int = Color.BLACK,
) : Element()