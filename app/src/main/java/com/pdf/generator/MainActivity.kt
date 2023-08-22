package com.pdf.generator

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pdf.pdf_generator.PDFGenerator
import com.pdf.pdf_generator.PdfMargin
import com.pdf.pdf_generator.elements.ImageElement
import com.pdf.pdf_generator.elements.LineElement
import com.pdf.pdf_generator.elements.ParagraphElement
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val isExternalStorageAvailable = Environment.isExternalStorageEmulated()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val pdfFile = if (isExternalStorageAvailable) getFile("file") else null
            val text1 = ParagraphElement("This is a short text 1")
            val text2 =
                ParagraphElement(
                    "This is a long text 1 good night  hello world nice to meet you all, good bay ",
                    color = getColor(R.color.white)
                )
            val bitmap =
                BitmapFactory.decodeResource(resources, R.drawable.avatar)
            val image1 = ImageElement(bitmap = bitmap)
            val image2 = ImageElement(bitmap = bitmap, alpha = 25, width = 100, height = 100)
            val line = LineElement()


            pdfFile?.let {
                val pdf = PDFGenerator.Builder(it)
                    .setMargin(PdfMargin(all = 50f))
                    .setBackgroundColor(getColor(R.color.purple_200))
                    .build()
                pdf.addElement(text1)
                pdf.addElement(text2)
                pdf.addElement(image1)
                pdf.addElement(image2)
                pdf.addElement(text2)
                pdf.addElement(text2)
                pdf.addElement(text2)
                pdf.addElement(line)
                repeat(15) {
                    pdf.addElement(text2)
                }
                repeat(65) {
                    pdf.addElement(line)
                }
                pdf.generate()
            }
        }
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted
            // Your code for accessing storage here
        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 1)
        }
    }


    private fun getFile(name: String): File {
        var pdfFile: File
        try {
            var fileName = "${name}.pdf"
            val filePath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!filePath.exists()) {
                filePath.mkdir()
            }

            pdfFile = File(filePath, fileName)
            var count = 1
            while (pdfFile.exists()) {
                fileName = "${name}(" + count + ").pdf"
                pdfFile = File(filePath, fileName)
                count++
            }
            pdfFile.createNewFile()
        } catch (e: IOException) {
            throw e
        }
        return pdfFile
    }

}