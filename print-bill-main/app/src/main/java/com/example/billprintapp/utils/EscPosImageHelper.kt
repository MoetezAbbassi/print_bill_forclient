package com.example.billprintapp.utils

import android.graphics.*
import java.io.ByteArrayOutputStream

object EscPosImageHelper {

    fun bitmapToEscPos(original: Bitmap): ByteArray {
        val width = 576
        val scaled = Bitmap.createScaledBitmap(original, width, (original.height * width) / original.width, false)
        val bwBitmap = toMonochrome(scaled)

        val stream = ByteArrayOutputStream()

        // ✅ Init ESC/POS to prevent junk characters
        stream.write(byteArrayOf(0x1B, 0x40)) // ESC @

        // ✅ Start raster graphics mode
        stream.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00))

        val bytesPerRow = (bwBitmap.width + 7) / 8
        val widthLow = bytesPerRow % 256
        val widthHigh = bytesPerRow / 256
        val heightLow = bwBitmap.height % 256
        val heightHigh = bwBitmap.height / 256

        stream.write(byteArrayOf(widthLow.toByte(), widthHigh.toByte(), heightLow.toByte(), heightHigh.toByte()))

        for (y in 0 until bwBitmap.height) {
            for (x in 0 until bwBitmap.width step 8) {
                var b = 0
                for (bit in 0..7) {
                    val px = x + bit
                    if (px < bwBitmap.width) {
                        val color = bwBitmap.getPixel(px, y)
                        val red = Color.red(color)
                        if (red == 0) {
                            b = b or (1 shl (7 - bit))
                        }
                    }
                }
                stream.write(b)
            }
        }

        // ✅ Line feeds to ensure paper feed stops
        stream.write(0x0A)
        stream.write(0x0A)

        return stream.toByteArray()
    }

    private fun toMonochrome(src: Bitmap): Bitmap {
        val bw = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bw)
        val paint = Paint()
        val matrix = ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(src, 0f, 0f, paint)

        for (x in 0 until bw.width) {
            for (y in 0 until bw.height) {
                val gray = Color.red(bw.getPixel(x, y))
                val bwColor = if (gray < 160) Color.BLACK else Color.WHITE
                bw.setPixel(x, y, bwColor)
            }
        }

        return bw
    }
}
