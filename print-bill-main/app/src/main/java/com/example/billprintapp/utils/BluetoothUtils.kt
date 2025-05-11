package com.example.billprintapp.utils

import android.bluetooth.BluetoothAdapter

fun isBluetoothEnabled(): Boolean {
    val adapter = BluetoothAdapter.getDefaultAdapter()
    return adapter?.isEnabled == true
}
