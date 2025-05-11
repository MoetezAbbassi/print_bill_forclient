package com.example.billprintapp.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun generatePdf(context: Context, content: String) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
    val page = document.startPage(pageInfo)

    val canvas = page.canvas
    val paint = Paint().apply {
        textSize = 12f
        color = Color.BLACK
    }

    val lines = content.split("\n")
    var y = 25
    lines.forEach {
        canvas.drawText(it, 10f, y.toFloat(), paint)
        y += 20
    }

    document.finishPage(page)

    val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    dir?.mkdirs()
    val file = File(dir, "receipt_${System.currentTimeMillis()}.pdf")
    document.writeTo(FileOutputStream(file))
    document.close()

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(intent)
}
