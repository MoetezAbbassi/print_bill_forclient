package com.example.billprintapp.utils

import android.graphics.*
import java.io.ByteArrayOutputStream

object EscPosImageHelper {

    fun bitmapToEscPos(bitmap: Bitmap): ByteArray {
        val scaled = scaleToWidth(bitmap, 576)
        val bw = toBlackAndWhite(scaled)

        val stream = ByteArrayOutputStream()
        stream.write(byteArrayOf(0x1B, 0x40)) // Initialize printer
        stream.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00)) // Raster bit image command

        val width = bw.width
        val height = bw.height
        val widthBytes = (width + 7) / 8
        val widthLow = widthBytes % 256
        val widthHigh = widthBytes / 256
        val heightLow = height % 256
        val heightHigh = height / 256

        stream.write(byteArrayOf(widthLow.toByte(), widthHigh.toByte(), heightLow.toByte(), heightHigh.toByte()))

        for (y in 0 until height) {
            for (x in 0 until width step 8) {
                var byte = 0
                for (bit in 0..7) {
                    val pixelX = x + bit
                    if (pixelX < width) {
                        val color = bw.getPixel(pixelX, y)
                        val r = (color shr 16) and 0xff
                        if (r < 128) {
                            byte = byte or (1 shl (7 - bit))
                        }
                    }
                }
                stream.write(byte)
            }
        }

        stream.write(byteArrayOf(0x0A)) // Line feed
        return stream.toByteArray()
    }

    fun scaleToWidth(bitmap: Bitmap, targetWidth: Int): Bitmap {
        val ratio = targetWidth.toFloat() / bitmap.width
        val height = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, targetWidth, height, false)
    }

    private fun toBlackAndWhite(original: Bitmap): Bitmap {
        val bwBitmap = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bwBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(original, 0f, 0f, paint)

        for (x in 0 until bwBitmap.width) {
            for (y in 0 until bwBitmap.height) {
                val pixel = bwBitmap.getPixel(x, y)
                val red = (pixel shr 16) and 0xff
                bwBitmap.setPixel(x, y, if (red < 128) Color.BLACK else Color.WHITE)
            }
        }

        return bwBitmap
    }
}
