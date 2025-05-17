package com.example.billprintapp.utils

import android.graphics.*
import com.example.billprintapp.models.EditableItem
import java.text.SimpleDateFormat
import java.util.*

object ReceiptBitmapBuilder {

    fun buildBitmap(customerName: String, items: List<EditableItem>): Bitmap {
        val width = 576
        val maxHeight = 2500 // 🆙 Gives space for QR and extra lines
        val baseBitmap = Bitmap.createBitmap(width, maxHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(baseBitmap)
        canvas.drawColor(Color.WHITE) // ✅ prevent black bar

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 28f
            isAntiAlias = false
            typeface = Typeface.MONOSPACE
        }

        var y = 40

        fun drawCentered(text: String, size: Float = 28f) {
            paint.textSize = size
            val textWidth = paint.measureText(text)
            canvas.drawText(text, (width - textWidth) / 2f, y.toFloat(), paint)
            y += 40
        }

        fun drawLeftRight(left: String, right: String) {
            paint.textSize = 24f
            canvas.drawText(left, 20f, y.toFloat(), paint)
            val rightWidth = paint.measureText(right)
            canvas.drawText(right, (width - rightWidth - 20), y.toFloat(), paint)
            y += 36
        }

        drawCentered("ICE")
        drawCentered("مؤسسة")

        // Compact details
        paint.textSize = 20f
        val lines = listOf("Dammam, SA", "VAT: 300836003", "Tel: 0500000000")
        for (line in lines) {
            val textWidth = paint.measureText(line)
            canvas.drawText(line, (width - textWidth) / 2f, y.toFloat(), paint)
            y += 24
        }

        y += 10
        canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        y += 20

        drawCentered("  ")
        drawCentered("Simplified Tax Invoice")
        drawCentered("فاتورة ضريبية مبسطة")

        y += 10
        canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        y += 30

        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        drawLeftRight("Invoice Date:", date)
        drawLeftRight("Customer:", customerName)

        y += 20
        canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        y += 40

        var subtotal = 0.0
        drawLeftRight("Item", "Qty x Price")
        items.forEach {
            val total = it.price * it.quantity
            subtotal += total
            drawLeftRight(it.name, "${it.quantity} x ${"%.2f".format(it.price)}")
        }

        val vat = subtotal * 0.15
        val total = subtotal + vat

        y += 30
        canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        y += 40

        drawLeftRight("Subtotal", "%.2f".format(subtotal) + " ﷼")
        drawLeftRight("VAT (15%)", "%.2f".format(vat) + " ﷼")
        drawLeftRight("Total", "%.2f".format(total) + " ﷼")

        y += 30
        canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        y += 40

        drawCentered("Thank you!", 24f)

        // QR Code
        val qr = QrGenerator.generateQr("https://ice.pos/receipt?id=123456", 200)
        canvas.drawBitmap(qr, (width - 200) / 2f, y.toFloat(), null)
        y += 240

        y += 100 // Final padding for safety

        val finalHeight = y.coerceAtMost(maxHeight).coerceAtLeast(1)
        return Bitmap.createBitmap(baseBitmap, 0, 0, width, finalHeight)
    }
}
