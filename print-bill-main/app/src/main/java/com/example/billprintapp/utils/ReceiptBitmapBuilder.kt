package com.example.billprintapp.utils

import android.graphics.*
import com.example.billprintapp.models.EditableItem
import java.text.SimpleDateFormat
import java.util.*

object ReceiptBitmapBuilder {

    fun buildReceiptBitmap(customerName: String, items: List<EditableItem>): Bitmap {
        val width = 576
        val maxHeight = 2500
        val baseBitmap = Bitmap.createBitmap(width, maxHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(baseBitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = false
            typeface = Typeface.MONOSPACE
        }

        var y = 40

        fun drawCentered(text: String, size: Float = 28f, spacing: Int = 40) {
            paint.textSize = size
            val textWidth = paint.measureText(text)
            canvas.drawText(text, (width - textWidth) / 2f, y.toFloat(), paint)
            y += spacing
        }

        fun drawLeftRight(left: String, right: String, size: Float = 24f, spacing: Int = 36) {
            paint.textSize = size
            canvas.drawText(left, 20f, y.toFloat(), paint)
            val rightWidth = paint.measureText(right)
            canvas.drawText(right, width - rightWidth - 20f, y.toFloat(), paint)
            y += spacing
        }

        // 🧊 Header
        drawCentered("ICE", 32f)
        drawCentered("مؤسسة", 28f)

        paint.textSize = 20f
        listOf("Dammam, SA", "VAT: 300836003", "Tel: 500000000").forEach {
            val tw = paint.measureText(it)
            canvas.drawText(it, (width - tw) / 2f, y.toFloat(), paint)
            y += 24
        }

        y += 8
        canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        y += 20

        drawCentered(" ")
        drawCentered("Simplified Tax Invoice", 24f)
        drawCentered("فاتورة ضريبية مبسطة", 22f)
        drawCentered(" ")

        y += 8
        canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        y += 30

        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val invoiceId = (100000..999999).random().toString()

        drawLeftRight("Invoice #", invoiceId)
        drawLeftRight("Date", date)
        drawLeftRight("Customer", customerName)

        y += 10
        canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        y += 30

        drawLeftRight("Item", "Qty x Price", 24f)

        var subtotal = 0.0
        items.forEach {
            val line = "${it.quantity} x ${"%.2f".format(it.price)}"
            drawLeftRight(it.name.take(20), line)
            subtotal += it.quantity * it.price
        }

        y += 12
        canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        y += 30

        val vat = subtotal * 0.15
        val total = subtotal + vat

        drawLeftRight("Subtotal", "%.2f ﷼".format(subtotal))
        drawLeftRight("VAT (15%)", "%.2f ﷼".format(vat))
        drawLeftRight("TOTAL", "%.2f ﷼".format(total), 28f)

        y += 8
        canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        y += 30

        drawCentered("Thank you for your purchase! :)", 24f)

        val qr = QrGenerator.generateQr("https://ice.pos/receipt?id=$invoiceId", 200)
        canvas.drawBitmap(qr, (width - 200) / 2f, y.toFloat(), null)
        y += qr.height + 20
        y += 90

        return Bitmap.createBitmap(baseBitmap, 0, 0, width, y.coerceAtMost(maxHeight))
    }
}
