package com.example.billprintapp.ui

import android.content.Context
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
import com.example.billprintapp.models.EditableItem
import com.example.billprintapp.utils.PdfGenerator
import com.example.billprintapp.utils.PdfRendererUtil
import com.example.billprintapp.utils.ReceiptBuilder
import java.io.File

@Composable
fun ReceiptPrinterScreen(
    onPrintThermal: (String) -> Unit,
    onPrintPdf: (String) -> Unit,
    onSendPdfFile: (File) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("receipt_prefs", Context.MODE_PRIVATE) }

    var customerName by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf(EditableItem("ICE", 4.0, 1)) }
    var format by remember { mutableStateOf(prefs.getString("print_format", "PDF") ?: "PDF") }

    var previewText by remember { mutableStateOf("") }
    var pdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    var pdfFile by remember { mutableStateOf<File?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Customer Name:")
        TextField(
            value = customerName,
            onValueChange = { customerName = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Text("Items:")

        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                TextField(
                    value = item.name,
                    onValueChange = { items[index] = item.copy(name = it) },
                    label = { Text("Name") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = item.price.toString(),
                    onValueChange = {
                        items[index] = item.copy(price = it.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(100.dp)
                )
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = item.quantity.toString(),
                    onValueChange = {
                        items[index] = item.copy(quantity = it.toIntOrNull() ?: 0)
                    },
                    label = { Text("Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(80.dp)
                )
            }
        }

        Button(onClick = { items.add(EditableItem()) }, modifier = Modifier.padding(vertical = 8.dp)) {
            Text("Add Item")
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                if (customerName.isBlank() || items.none { it.name.isNotBlank() && it.quantity > 0 }) return@Button

                previewText = ReceiptBuilder.createFancyReceiptText(customerName, items)

                val file = PdfGenerator.generatePdf(context, previewText)
                pdfBitmap = PdfRendererUtil.renderPdfToBitmap(context, file)
                pdfFile = file
                showPreview = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send to PDF Printer")
        }

        Spacer(Modifier.height(12.dp))
    }

    // 👇 PDF Preview Dialog
    if (showPreview && pdfBitmap != null) {
        AlertDialog(
            onDismissRequest = { showPreview = false },
            confirmButton = {
                TextButton(onClick = {
                    showPreview = false
                    pdfFile?.let { onSendPdfFile(it) }
                }) {
                    Text("Print Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPreview = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Preview Receipt") },
            text = {
                Image(
                    bitmap = pdfBitmap!!.asImageBitmap(),
                    contentDescription = "PDF Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp, max = 600.dp)
                )
            }
        )
    }
}
