package com.example.billprintapp.utils

import android.graphics.Bitmap
import android.graphics.Color
import java.io.ByteArrayOutputStream

object EscPosImageHelper {

    fun bitmapToEscPos(bitmap: Bitmap): ByteArray {
        val bw = toBlackAndWhite(bitmap)
        return convertBitmapToRaster(bw)
    }

    private fun toBlackAndWhite(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val bwBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = src.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                val gray = (r + g + b) / 3
                val color = if (gray < 160) Color.BLACK else Color.WHITE
                bwBitmap.setPixel(x, y, color)
            }
        }

        return bwBitmap
    }

    private fun convertBitmapToRaster(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        val width = bitmap.width
        val height = bitmap.height

        val bytesPerRow = (width + 7) / 8
        val imageBytes = ByteArray(height * bytesPerRow)

        var idx = 0
        for (y in 0 until height) {
            for (xByte in 0 until bytesPerRow) {
                var b = 0
                for (bit in 0..7) {
                    val x = xByte * 8 + bit
                    if (x < width) {
                        val pixel = bitmap.getPixel(x, y)
                        val v = if (Color.red(pixel) == 0) 1 else 0
                        b = b or (v shl (7 - bit))
                    }
                }
                imageBytes[idx++] = b.toByte()
            }
        }

        // Add ESC/POS header for raster bit image
        val escpos = ByteArrayOutputStream()
        val nL = (bytesPerRow % 256).toByte()
        val nH = (bytesPerRow / 256).toByte()

        for (y in 0 until height) {
            escpos.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00, nL, nH, 0x01, 0x00)) // GS v 0
            escpos.write(imageBytes, y * bytesPerRow, bytesPerRow)
        }

        return escpos.toByteArray()
    }
}
