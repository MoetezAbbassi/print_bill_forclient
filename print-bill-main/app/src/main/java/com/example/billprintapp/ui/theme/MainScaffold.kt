package com.example.billprintapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
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
                        onPrintThermal = {},
                        onPrintPdf = {},
                        onSendPdfFile = {}
                    )
                    1 -> ReceiptHistoryScreen()
                }
            }
        }
    )
}
