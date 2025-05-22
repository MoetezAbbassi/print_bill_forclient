package com.example.billprintapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.billprintapp.db.ReceiptDatabase
import com.example.billprintapp.db.ReceiptEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReceiptHistoryScreen() {
    val context = LocalContext.current
    val dao = remember { ReceiptDatabase.getInstance(context).receiptDao() }
    val coroutineScope = rememberCoroutineScope()
    var receipts by remember { mutableStateOf<List<ReceiptEntity>>(emptyList()) }

    LaunchedEffect(true) {
        coroutineScope.launch {
            receipts = dao.getAll()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Past Receipts", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(receipts) { receipt ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Customer: ${receipt.customerName}")
                        Text("Total: ﷼%.2f".format(receipt.total))
                        Text("Date: ${formatDate(receipt.timestamp)}")
                    }
                }
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
