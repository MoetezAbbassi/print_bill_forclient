package com.example.billprintapp.utils

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.*
import android.print.pdf.PrintedPdfDocument
import java.io.FileOutputStream

class TextPrintDocumentAdapter(
    private val context: Context,
    private val text: String
) : PrintDocumentAdapter() {

    private var pdfDocument: PrintedPdfDocument? = null

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        pdfDocument = PrintedPdfDocument(context, newAttributes)

        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder("receipt.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .build()

        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        val page = pdfDocument!!.startPage(0)
        val canvas: Canvas = page.canvas
        val width = canvas.width

        val paint = Paint().apply {
            textSize = 14f
            color = Color.BLACK
            isAntiAlias = true
        }

        val boldPaint = Paint(paint).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        var y = 40f

        fun drawCentered(textLine: String, bold: Boolean = false, offsetY: Float = 22f) {
            val p = if (bold) boldPaint else paint
            val textWidth = p.measureText(textLine)
            canvas.drawText(textLine, (width - textWidth) / 2f, y, p)
            y += offsetY
        }

        fun drawDivider() {
            canvas.drawLine(20f, y, width - 20f, y, paint)
            y += 15f
        }

        drawCentered("⭐ GRAND ICE CREAM CO. ⭐", bold = true, offsetY = 28f)
        drawDivider()

        text.split("\n").forEach { raw ->
            val line = raw.trim()

            when {
                line.startsWith("<C>") -> drawCentered(line.removePrefix("<C>").removeSuffix("</C>"))
                line.startsWith("<B>") -> drawCentered(line.removePrefix("<B>").removeSuffix("</B>"), bold = true)
                line.startsWith("<QR>") -> drawCentered("[QR CODE]", bold = true)
                else -> {
                    canvas.drawText(line, 25f, y, paint)
                    y += 20f
                }
            }
        }

        pdfDocument!!.finishPage(page)

        try {
            pdfDocument!!.writeTo(FileOutputStream(destination.fileDescriptor))
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback.onWriteFailed(e.message)
        } finally {
            pdfDocument!!.close()
            pdfDocument = null
        }
    }
}
