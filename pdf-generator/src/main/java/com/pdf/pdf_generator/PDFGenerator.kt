package com.pdf.pdf_generator


import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.Page
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.pdf.pdf_generator.elements.Element
import com.pdf.pdf_generator.elements.ImageElement
import com.pdf.pdf_generator.elements.LineElement
import com.pdf.pdf_generator.elements.ParagraphElement
import com.pdf.pdf_generator.elements.ParagraphElement.TextAlign
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.RuntimeException
import kotlin.math.min


/**
 * This is a data class called `PdfMargin` which represents the margins of a PDF document.
 * The margins are specified using four floating-point values for the top, right, bottom, and left margins respectively.
 * @param top the size of the top margin in points.
 * @param right the size of the right margin in points.
 * @param bottom the size of the bottom margin in points.
 * @param left the size of the left margin in points.
 */
data class PdfMargin(
    val top: Float = 0f,
    val right: Float = 0f,
    val bottom: Float = 0f,
    val left: Float = 0f,
) {
    /**
     *The first secondary constructor takes a single floating-point value which is used for all four margins.
     * @param all the size of all four margins in points.
     * */
    constructor(all: Float = 0f) : this(all, all, all, all)

    /**
     *The second secondary constructor takes two floating-point values; one for the vertical margins (top and bottom) and another for the horizontal margins (left and right).
     * @param vertical the size of the top and bottom margins in points.
     * @param horizontal the size of the left and right margins in points.
     */
    constructor(vertical: Float = 0f, horizontal: Float = 0f) : this(
        top = vertical,
        bottom = vertical,
        left = horizontal,
        right = horizontal
    )
}

class PDFGenerator private constructor(builder: Builder) {
    private val file: File
    private val margin: PdfMargin
    private val pageSize: PdfPageSize
    private val waterMark: Bitmap?
    private val sign: Bitmap?
    private val backgroundColor: Int
    private val spaceBetweenElements: Float

    init {
        file = builder.file
        margin = builder.margin
        pageSize = builder.pageSize
        waterMark = builder.waterMark
        sign = builder.sign
        backgroundColor = builder.backgroundColor
        spaceBetweenElements = builder.spaceBetweenElements
    }

    class Builder(val file: File) {
        var margin: PdfMargin = PdfMargin(0f)
            private set
        var pageSize: PdfPageSize = PdfPageSize.A4
            private set
        var spaceBetweenElements: Float = 5f
            private set
        var waterMark: Bitmap? = null
            private set
        var sign: Bitmap? = null
            private set
        var backgroundColor = Color.WHITE
            private set


        fun setMargin(margin: PdfMargin) = apply { this.margin = margin }
        fun setPageSize(pageSize: PdfPageSize) = apply { this.pageSize = pageSize }
        fun setSign(sign: Bitmap) = apply { this.sign = sign }
        fun setWaterMark(waterMark: Bitmap) = apply { this.waterMark = waterMark }
        fun setBackgroundColor(backgroundColor: Int) =
            apply { this.backgroundColor = backgroundColor }

        fun setSpaceBetweenElements(space: Float) =
            apply { this.spaceBetweenElements = spaceBetweenElements }

        fun build() = PDFGenerator(this)

    }

    companion object {
        private const val CANVAS_ALPHA_MAX = 255
    }


    private val pageWidth = pageSize.width
    private val pageHeight = pageSize.height

    private val contentWidth = pageWidth - (margin.left + margin.right)
    private val contentHeight = pageHeight - (margin.top + margin.bottom)

    /**bottom offset to check if the text doesn't have space to fit the page from the bottom */
    private val pageBottomOffset = pageHeight - margin.bottom
    private val pageEndOffset = pageWidth - margin.right

    private val defaultXOffset = margin.left
    private val defaultYOffset = margin.top

    private var offsetX = defaultXOffset
    private var offsetY = defaultYOffset


    var pageNumbers = 0
        private set

    private val document = PdfDocument()
    private val paint = Paint()
    private var page = createPage()
    private var canvas: Canvas = page.canvas


    private fun createPage(): Page {
        try {
            pageNumbers++
            val pageInfo = PageInfo.Builder(pageWidth, pageHeight, pageNumbers).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawColor(backgroundColor)
            if (waterMark != null) {
                drawWaterMark(canvas = page.canvas, bitmap = waterMark, paint = paint)
            }
            if (sign != null) {
                drawSign(canvas = page.canvas, sign = sign, paint = paint)
            }
            return page
        } catch (e: Exception) {
            throw PageCreationException("Error creating page")
        }
    }

    private fun finishPageAndCreateNew() {
        document.finishPage(page)
        page = createPage()
        canvas = page.canvas
        offsetY = defaultYOffset
    }


    fun addElement(element: Element) {
        try {
            when (element) {
                is ParagraphElement -> {
                    drawParagraph(element)
                }
                is ImageElement -> {
                    drawImage(element)
                }
                is LineElement -> {
                    drawLine(element)
                }
            }
        } catch (e: RuntimeException) {
            throw ElementDrawingException("Error happened when draw element")
        }
    }

    fun addElements(elements: List<Element>) {
        try {
            elements.forEach { element ->
                when (element) {
                    is ParagraphElement -> {
                        drawParagraph(element)
                    }
                    is ImageElement -> {
                        drawImage(element)
                    }
                    is LineElement -> {
                        drawLine(element)
                    }
                }
            }
        } catch (e: RuntimeException) {
            throw ElementDrawingException("Error happened when draw elements")
        }
    }

    fun generate(): Boolean {
        return try {
            document.finishPage(page)
            document.writeTo(FileOutputStream(file))
            document.close()
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun drawImage(element: ImageElement) {
        val currentFitBottomOffset = element.height + offsetY
        if (currentFitBottomOffset > pageBottomOffset) {
            finishPageAndCreateNew()
        }

        val width = if (element.width > contentWidth)
            contentWidth else element.width
        val height = if (element.height > contentHeight)
            contentHeight else element.height
        val image = Bitmap.createScaledBitmap(
            element.bitmap, width.toInt(), height.toInt(), false
        )
        val imageOffsetX = when (element.align) {
            ImageElement.ImageAlign.LEFT -> {
                offsetX
            }

            ImageElement.ImageAlign.RIGHT -> {
                pageEndOffset - element.width
            }

            ImageElement.ImageAlign.CENTER -> {
                (pageWidth / 2) - (element.width / 2).toFloat()
            }
        }
        /**Save the canvas setting*/
        canvas.save()

        paint.alpha = calculateAlphaFromPercentage(element.alpha)
        canvas.drawBitmap(image, imageOffsetX, offsetY, paint)
        offsetY += element.height + spaceBetweenElements
        canvas.translate(offsetX, offsetY)
        /**Restore the canvas setting*/
        paint.alpha = CANVAS_ALPHA_MAX
        canvas.restore()
    }

    private fun drawParagraph(element: ParagraphElement) {
        paint.typeface = element.font
        paint.textSize = element.size
        paint.color = element.color
        val mTextPaint = TextPaint(paint)
        // get the static layout for the text and check if have space to fit the text
        val textLayout = getStaticLayout(element, mTextPaint)

        /** check if the text fit the current page or create new */
        val linesHeight = textLayout.height
        val currentFitBottomOffset = offsetY + spaceBetweenElements + linesHeight
        if (currentFitBottomOffset > pageBottomOffset) {
            finishPageAndCreateNew()
        }
        /** start drawing */
        canvas.save()
        canvas.translate(offsetX, offsetY)
        textLayout.draw(canvas)
        offsetY += (spaceBetweenElements + linesHeight)
        /**Restore the canvas setting*/
        canvas.restore()
    }

    private fun drawLine(element: LineElement) {

        /** check if the text fit the current page or create new */
        val lineHeight = element.width
        val currentFitBottomOffset = offsetY + spaceBetweenElements + lineHeight
        if (currentFitBottomOffset > pageBottomOffset) {
            finishPageAndCreateNew()
        }
        paint.strokeWidth = lineHeight
        paint.color = element.color
        /** start drawing */
        canvas.save()
        canvas.drawLine(offsetX, offsetY, pageEndOffset, offsetY, paint)
        offsetY += (spaceBetweenElements + element.width)
        canvas.translate(offsetX, offsetY)
        /**Restore the canvas setting*/
        canvas.restore()
    }


    private fun drawWaterMark(
        canvas: Canvas,
        paint: Paint,
        bitmap: Bitmap,
    ) {
        canvas.save()

        paint.alpha = 65 // set the alpha value to 65 (25% opaque)

        val scale = min(contentWidth / bitmap.width, contentHeight / bitmap.height)
        val matrix = Matrix()

        matrix.postScale(scale, scale)
        matrix.postRotate(-45f)


        canvas.translate(contentWidth.times(.1f), contentHeight.times(.7f))
        canvas.drawBitmap(bitmap, matrix, paint)

        paint.alpha = 255 // return the alpha value to 255 (100% opaque)
        canvas.translate(offsetX, offsetY)
        /**Restore the canvas setting*/
        canvas.restore()
    }

    private fun calculateAlphaFromPercentage(percentage: Int): Int {
        val clampedPercentage =
            percentage.coerceIn(0, 100) // Ensure the percentage is within the range 0-100
        return (clampedPercentage / 100.0 * CANVAS_ALPHA_MAX).toInt()
    }

    private fun drawSign(
        canvas: Canvas,
        paint: Paint,
        sign: Bitmap,
    ) {
        canvas.save()
        val signHeight = 30
        val signWidth = 150
        val signSmall = Bitmap.createScaledBitmap(sign, signWidth, signHeight, false)
        val matrix = Matrix()
        canvas.translate(
            (pageWidth - signWidth - 10).toFloat(),
            (pageHeight - signHeight).toFloat()
        )
        canvas.drawBitmap(signSmall, matrix, paint)
        /**Restore the canvas setting*/
        canvas.translate(offsetX, offsetY)
        canvas.restore()
    }

    private fun getStaticLayout(
        element: ParagraphElement,
        textPaint: TextPaint,
    ): StaticLayout {

        val align = when (element.align) {
            TextAlign.DEFAULT -> {
                Layout.Alignment.ALIGN_NORMAL
            }

            TextAlign.CENTER -> {
                Layout.Alignment.ALIGN_CENTER
            }

            TextAlign.OPPOSITE -> {
                Layout.Alignment.ALIGN_OPPOSITE
            }
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(
                element.text,
                0,
                element.text.length,
                textPaint,
                contentWidth.toInt()
            )
                .setAlignment(align)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                element.text,
                textPaint,
                contentWidth.toInt(),
                align,
                1.0f,
                0.0f,
                false
            )
        }
    }
}


sealed class Fonts(val font: Typeface) {
    object Default : Fonts(Typeface.DEFAULT)
    object Monospace : Fonts(Typeface.MONOSPACE)
    object Serif : Fonts(Typeface.SERIF)
    object SansSerif : Fonts(Typeface.SANS_SERIF)

    /**
     * @param font the font of custom font style
     * Example {@link androidx.core.content.res.ResourcesCompat}
     *  ResourcesCompat.getFont(context, R.font.montserrat_regular)
     * */
    class Custom(font: Typeface) : Fonts(font)
}