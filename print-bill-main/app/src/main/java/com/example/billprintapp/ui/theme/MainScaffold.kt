package com.example.billprintapp.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.billprintapp.utils.BluetoothPrinterHelper
import com.example.billprintapp.utils.PdfRendererUtil
import com.example.billprintapp.ui.ReceiptHistoryScreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ICE POS") }
            )
        },
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Home") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("History") })
                }

                when (selectedTab) {
                    0 -> ReceiptPrinterScreen(
                        onSendPdfFile = { file ->
                            val bitmap = PdfRendererUtil.renderPdfToBitmap(context, file)
                            BluetoothPrinterHelper.showDevicePicker(context) { _ ->
                                BluetoothPrinterHelper.printBitmap(context, bitmap)
                            }
                        }
                    )
                    1 -> ReceiptHistoryScreen()
                }
            }
        }
    )
}
