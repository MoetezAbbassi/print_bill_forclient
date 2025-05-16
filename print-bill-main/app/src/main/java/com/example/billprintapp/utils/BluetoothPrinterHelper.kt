package com.example.billprintapp.utils

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import java.io.OutputStream
import java.util.*

object BluetoothPrinterHelper {

    private var selectedDevice: BluetoothDevice? = null

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

        val deviceNames = pairedDevices.map { it.name ?: it.address }
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Printer")
        builder.setItems(deviceNames.toTypedArray()) { _, which ->
            selectedDevice = pairedDevices[which]
            onDeviceSelected(selectedDevice!!)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    fun printBitmap(context: Context, bitmap: Bitmap) {
        val device = selectedDevice
        if (device == null) {
            Toast.makeText(context, "No printer selected", Toast.LENGTH_SHORT).show()
            return
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

            Toast.makeText(context, "Receipt sent to ${device.name}", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Print failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
