package com.example.billprintapp.utils

import android.app.AlertDialog
import android.bluetooth.*
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import java.io.OutputStream
import java.util.*

object BluetoothPrinterHelper {

    private var selectedDevice: BluetoothDevice? = null
    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    fun showDevicePicker(context: Context, onDeviceSelected: (BluetoothDevice) -> Unit) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null || !adapter.isEnabled) {
            Toast.makeText(context, "Bluetooth not available or off", Toast.LENGTH_LONG).show()
            return
        }

        val pairedDevices = adapter.bondedDevices.toList()
        if (pairedDevices.isEmpty()) {
            Toast.makeText(context, "No paired devices found", Toast.LENGTH_LONG).show()
            return
        }

        val names = pairedDevices.map { it.name ?: it.address }

        AlertDialog.Builder(context)
            .setTitle("Select Printer")
            .setItems(names.toTypedArray()) { _, which ->
                selectedDevice = pairedDevices[which]
                onDeviceSelected(selectedDevice!!)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun printBitmap(context: Context, bitmap: Bitmap) {
        val device = selectedDevice ?: run {
            Toast.makeText(context, "No printer selected", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (socket == null || !socket!!.isConnected) {
                val uuid = device.uuids?.firstOrNull()?.uuid
                    ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
                socket = device.createRfcommSocketToServiceRecord(uuid)
                socket!!.connect()
                outputStream = socket!!.outputStream
            }

            // ✅ Convert bitmap to black/white ESC/POS
            val bytes = EscPosImageHelper.bitmapToEscPos(bitmap)
            outputStream!!.write(bytes)
            outputStream!!.flush()

            Toast.makeText(context, "Printed to ${device.name}", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Print failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
