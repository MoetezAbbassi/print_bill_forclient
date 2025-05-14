package com.example.billprintapp.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    fun generatePdf(context: Context, content: String): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 800, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint().apply {
            textSize = 12f
            color = Color.BLACK
        }

        val lines = content.split("\n")
        var y = 25
        for (line in lines) {
            canvas.drawText(line, 10f, y.toFloat(), paint)
            y += 20
        }

        document.finishPage(page)

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        dir?.mkdirs()
        val file = File(dir, "receipt_${System.currentTimeMillis()}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()

        return file
    }
}
