package com.example.billprintapp.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.billprintapp.db.ReceiptDatabase
import com.example.billprintapp.db.ReceiptEntity
import com.example.billprintapp.models.EditableItem
import com.example.billprintapp.utils.BluetoothPrinterHelper
import com.example.billprintapp.utils.PdfGenerator
import com.example.billprintapp.utils.PdfRendererUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPrinterScreen(
    onPrintThermal: (String) -> Unit,
    onPrintPdf: (String) -> Unit,
    onSendPdfFile: (java.io.File) -> Unit
) {
    val context = LocalContext.current
    var customerName by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf(EditableItem("ICE", 4.0, 1)) }

    var showPreview by remember { mutableStateOf(false) }
    var pdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pdfFile by remember { mutableStateOf<java.io.File?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Customer Name:")
        TextField(
            value = customerName,
            onValueChange = { customerName = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        items.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                TextField(
                    value = item.name,
                    onValueChange = { items[index] = item.copy(name = it) },
                    label = { Text("Name") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = item.price.toString(),
                    onValueChange = { items[index] = item.copy(price = it.toDoubleOrNull() ?: 0.0) },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(90.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = item.quantity.toString(),
                    onValueChange = { items[index] = item.copy(quantity = it.toIntOrNull() ?: 0) },
                    label = { Text("Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(80.dp)
                )
            }
        }

        Button(onClick = { items.add(EditableItem()) }, modifier = Modifier.padding(vertical = 8.dp)) {
            Text("Add Item")
        }

        Button(
            onClick = {
                val file = PdfGenerator.generateSmartReceiptPdf(context, customerName, items)
                pdfBitmap = PdfRendererUtil.renderPdfToBitmap(context, file)
                pdfFile = file
                showPreview = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Preview & Print")
        }
    }

    if (showPreview && pdfBitmap != null && pdfFile != null) {
        AlertDialog(
            onDismissRequest = { showPreview = false },
            confirmButton = {
                TextButton(onClick = {
                    showPreview = false

                    // Save receipt to database
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = ReceiptDatabase.getInstance(context).receiptDao()
                        dao.insert(
                            ReceiptEntity(
                                customerName = customerName,
                                total = items.sumOf { it.price * it.quantity },
                                timestamp = System.currentTimeMillis(),
                                receiptText = "Printed via ICE POS"
                            )
                        )
                    }

                    // Pick printer & print
                    BluetoothPrinterHelper.showDevicePicker(context) {
                        BluetoothPrinterHelper.printBitmap(context, pdfBitmap!!)
                    }

                }) {
                    Text("Print")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPreview = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Receipt Preview") },
            text = {
                Image(
                    bitmap = pdfBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp, max = 600.dp)
                )
            }
        )
    }
}
