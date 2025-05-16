package com.example.billprintapp.utils

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

object EscPosImageHelper {

    fun bitmapToEscPos(bitmap: Bitmap): ByteArray {
        val scaled = scaleToWidth(bitmap, 576) // width for 80mm printer

        val stream = ByteArrayOutputStream()
        stream.write(byteArrayOf(0x1B, 0x40)) // Initialize printer
        stream.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00)) // Raster bit image command

        val width = scaled.width
        val height = scaled.height
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
                        val pixel = scaled.getPixel(pixelX, y)
                        val gray = (0.3 * ((pixel shr 16) and 0xFF) +
                                0.59 * ((pixel shr 8) and 0xFF) +
                                0.11 * (pixel and 0xFF)).toInt()
                        if (gray < 128) {
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

    private fun scaleToWidth(original: Bitmap, targetWidth: Int): Bitmap {
        val ratio = targetWidth.toFloat() / original.width
        val height = (original.height * ratio).toInt()
        return Bitmap.createScaledBitmap(original, targetWidth, height, true)
    }
}
