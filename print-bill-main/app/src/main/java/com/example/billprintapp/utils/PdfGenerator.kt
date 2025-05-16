package com.example.billprintapp.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.billprintapp.models.EditableItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generateSmartReceiptPdf(context: Context, customerName: String, items: List<EditableItem>): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 800, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas
        val width = canvas.width

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val boldPaint = Paint(paint).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val center = width / 2f
        var y = 30f

        fun drawCentered(text: String, yPos: Float = y, bold: Boolean = false) {
            val p = if (bold) boldPaint else paint
            val textWidth = p.measureText(text)
            canvas.drawText(text, center - textWidth / 2f, yPos, p)
            y += 18f
        }

        fun drawLeftRight(left: String, right: String) {
            canvas.drawText(left, 10f, y, paint)
            val rightWidth = paint.measureText(right)
            canvas.drawText(right, width - 10f - rightWidth, y, paint)
            y += 18f
        }

        fun drawLine() {
            canvas.drawLine(10f, y, width - 10f, y, paint)
            y += 10f
        }

        // Header
        drawLine()
        drawCentered("Simplified Tax Invoice", bold = true)
        drawCentered("فاتورة ضريبية مبسطة", bold = true)
        drawLine()

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateStr = sdf.format(Date())
        val invoiceId = (100000..999999).random().toString()

        drawLeftRight("Invoice #:", invoiceId)
        drawLeftRight("Date:", dateStr)
        drawLine()

        // Table header
        val columns = listOf("Item", "Qty", "Price", "Total")
        val colWidths = listOf(80f, 40f, 60f, 60f)
        var colX = 10f
        columns.forEachIndexed { i, title ->
            canvas.drawText(title, colX, y, boldPaint)
            colX += colWidths[i]
        }
        y += 18f

        var subtotal = 0.0
        for (item in items) {
            if (item.name.isBlank() || item.quantity <= 0) continue
            val total = item.quantity * item.price
            colX = 10f
            val values = listOf(
                item.name.take(10),
                item.quantity.toString(),
                "﷼%.2f".format(item.price),
                "﷼%.2f".format(total)
            )
            values.forEachIndexed { i, value ->
                canvas.drawText(value, colX, y, paint)
                colX += colWidths[i]
            }
            y += 18f
            subtotal += total
        }

        y += 10f
        drawLine()

        val vat = subtotal * 0.15
        val total = subtotal + vat

        drawLeftRight("Subtotal:", "﷼%.2f".format(subtotal))
        drawLeftRight("VAT (15%):", "﷼%.2f".format(vat))
        drawLeftRight("TOTAL:", "﷼%.2f".format(total))
        drawLine()

        // QR code with receipt info
        val qrContent = """
            Invoice#: $invoiceId
            Customer: $customerName
            Total: ﷼%.2f
            Date: $dateStr
        """.trimIndent().format(total)

        try {
            val qrBitmap = QrGenerator.generateQr(qrContent, 160)
            canvas.drawBitmap(qrBitmap, center - qrBitmap.width / 2f, y, null)
            y += qrBitmap.height + 20f
        } catch (e: Exception) {
            drawCentered("QR code error", bold = true)
        }

        drawCentered("Thank you for your purchase!")

        doc.finishPage(page)

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(dir, "receipt_${System.currentTimeMillis()}.pdf")
        doc.writeTo(FileOutputStream(file))
        doc.close()

        return file
    }
}
