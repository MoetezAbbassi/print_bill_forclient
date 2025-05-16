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
import com.example.billprintapp.models.EditableItem
import com.example.billprintapp.utils.PdfGenerator
import com.example.billprintapp.utils.PdfRendererUtil
import java.io.File

@Composable
fun ReceiptPrinterScreen(
    onPrintThermal: (String) -> Unit,
    onPrintPdf: (String) -> Unit,
    onSendPdfFile: (File) -> Unit
) {
    val context = LocalContext.current
    var customerName by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf(EditableItem("ICE", 4.0, 1)) }
    var showPreview by remember { mutableStateOf(false) }
    var pdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pdfFile by remember { mutableStateOf<File?>(null) }

    Column(Modifier.padding(16.dp)) {
        Text("Customer Name:")
        TextField(value = customerName, onValueChange = { customerName = it }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))
        items.forEachIndexed { index, item ->
            Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = item.name,
                    onValueChange = { items[index] = item.copy(name = it) },
                    label = { Text("Name") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = item.price.toString(),
                    onValueChange = { items[index] = item.copy(price = it.toDoubleOrNull() ?: 0.0) },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(90.dp)
                )
                Spacer(Modifier.width(8.dp))
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

        Button(onClick = {
            val file = PdfGenerator.generateSmartReceiptPdf(context, customerName, items)
            pdfBitmap = PdfRendererUtil.renderPdfToBitmap(context, file)
            pdfFile = file
            showPreview = true
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Preview & Print")
        }

        pdfBitmap?.let {
            if (showPreview && pdfFile != null) {
                AlertDialog(
                    onDismissRequest = { showPreview = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showPreview = false
                            onSendPdfFile(pdfFile!!)
                        }) { Text("Send to ESC POS") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPreview = false }) { Text("Cancel") }
                    },
                    title = { Text("PDF Preview") },
                    text = {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 600.dp)
                        )
                    }
                )
            }
        }
    }
}
