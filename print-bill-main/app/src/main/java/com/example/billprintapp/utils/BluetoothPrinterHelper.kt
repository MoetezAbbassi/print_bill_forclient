package com.example.billprintapp.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import java.io.OutputStream
import java.util.*

object BluetoothPrinterHelper {

    fun printBitmap(context: Context, bitmap: Bitmap) {
        val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            ?: return Toast.makeText(context, "Bluetooth not supported", Toast.LENGTH_SHORT).show()

        if (!adapter.isEnabled) {
            return Toast.makeText(context, "Bluetooth is OFF", Toast.LENGTH_SHORT).show()
        }

        val device: BluetoothDevice? = adapter.bondedDevices.find {
            it.name.contains("printer", true) || it.name.contains("POS", true)
        }

        if (device == null) {
            return Toast.makeText(context, "No paired printer found", Toast.LENGTH_LONG).show()
        }

        try {
            val uuid = device.uuids?.get(0)?.uuid ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()

            val stream: OutputStream = socket.outputStream
            val bytes = EscPosImageHelper.bitmapToEscPos(bitmap)
            stream.write(bytes)
            stream.flush()
            stream.close()
            socket.close()

            Toast.makeText(context, "Receipt sent to printer", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Print failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
