package com.example.billprintapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.billprintapp.models.Customer
import com.example.billprintapp.models.Item
import com.example.billprintapp.ui.theme.components.DropdownMenuDemo
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

val customers = listOf(
    Customer("Alice"),
    Customer("Bob"),
    Customer("Charlie")
)

val items = listOf(
    Item("Item A", 10.0),
    Item("Item B", 15.0),
    Item("Item C", 8.5)
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestBluetoothPermissions()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ReceiptPrinterScreen(
                        onPrintThermal = { receipt ->
                            val printer = connectToPrinter(this)
                            if (printer != null) {
                                sendToPrinterService(receipt)
                            }
                        },
                        onPrintPdf = { receipt ->
                            generatePdf(this, receipt)
                        }
                    )
                }
            }
        }
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
            ActivityCompat.requestPermissions(this, permissions, 101)
        }
    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendToPrinterService(receiptText: String) {
        try {
            val intent = Intent("com.escpos.ACTION_PRINT").apply {
                putExtra("text", receiptText)
                putExtra("font_size", "small")
                putExtra("alignment", "center")
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to send to printer", Toast.LENGTH_SHORT).show()
        }
    }

    private fun connectToPrinter(context: Context): BluetoothDevice? {
        val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        if (adapter == null) {
            Toast.makeText(context, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            return null
        }

        if (!adapter.isEnabled) {
            Toast.makeText(context, "Bluetooth is OFF. Please enable it.", Toast.LENGTH_SHORT).show()
            return null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Missing BLUETOOTH_CONNECT permission", Toast.LENGTH_LONG).show()
            return null
        }

        val pairedDevices = try {
            adapter.bondedDevices
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permission error: ${e.message}", Toast.LENGTH_SHORT).show()
            return null
        }

        if (pairedDevices.isEmpty()) {
            Toast.makeText(context, "No paired devices found", Toast.LENGTH_SHORT).show()
            return null
        }

        val printer = pairedDevices.find {
            it.name.contains("printer", ignoreCase = true) || it.name.contains("pos", ignoreCase = true)
        }

        return if (printer != null) {
            Toast.makeText(context, "Connected to: ${printer.name}", Toast.LENGTH_SHORT).show()
            printer
        } else {
            Toast.makeText(context, "No printer found in paired devices", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun generatePdf(context: Context, content: String) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint().apply {
            textSize = 12f
            color = Color.BLACK
        }

        val lines = content.split("\n")
        var y = 25
        lines.forEach {
            canvas.drawText(it, 10f, y.toFloat(), paint)
            y += 20
        }

        document.finishPage(page)

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        dir?.mkdirs()
        val file = File(dir, "receipt_${System.currentTimeMillis()}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    }
}

@Composable
fun ReceiptPrinterScreen(
    onPrintThermal: (String) -> Unit,
    onPrintPdf: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedCustomer by remember { mutableStateOf(customers.first()) }
    val quantities = remember { mutableStateListOf(0, 0, 0) }
    var status by remember { mutableStateOf("Idle") }
    var loading by remember { mutableStateOf(false) }
    var startPrint by remember { mutableStateOf(false) }
    var format by remember { mutableStateOf("Thermal") }

    LaunchedEffect(startPrint) {
        if (startPrint) {
            status = "Preparing receipt..."
            loading = true
            delay(1500)
            val receipt = createReceiptText(selectedCustomer, items, quantities)
            if (format == "Thermal") onPrintThermal(receipt) else onPrintPdf(receipt)
            status = "Printed successfully!"
            loading = false
            startPrint = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Select Customer:")
        DropdownMenuDemo(selectedCustomer) { selectedCustomer = it }

        Spacer(Modifier.height(16.dp))
        Text("Enter Quantities:")

        items.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("${item.name} ($${item.price})", modifier = Modifier.weight(1f))
                TextField(
                    value = quantities[index].toString(),
                    onValueChange = {
                        quantities[index] = it.toIntOrNull() ?: 0
                    },
                    modifier = Modifier.width(80.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Print Format:")
        Row {
            RadioButton(selected = format == "Thermal", onClick = { format = "Thermal" })
            Text("Thermal", modifier = Modifier.padding(end = 16.dp))
            RadioButton(selected = format == "PDF", onClick = { format = "PDF" })
            Text("PDF")
        }

        Spacer(Modifier.height(16.dp))
        Text(status)

        if (loading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { startPrint = true }) {
                Text("Print Receipt")
            }
        }
    }
}

fun createReceiptText(customer: Customer, items: List<Item>, quantities: List<Int>): String {
    val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date())

    val sb = StringBuilder()
    sb.appendLine("FAST BILLS")
    sb.appendLine("--------------------------")
    sb.appendLine("Customer: ${customer.name}")
    sb.appendLine("Date: $dateStr")
    sb.appendLine("--------------------------")

    var subtotal = 0.0
    items.forEachIndexed { index, item ->
        val qty = quantities[index]
        if (qty > 0) {
            val totalItemPrice = qty * item.price
            sb.appendLine("${item.name} x$qty   $${"%.2f".format(totalItemPrice)}")
            subtotal += totalItemPrice
        }
    }

    val tax = subtotal * 0.15
    val total = subtotal + tax

    sb.appendLine("--------------------------")
    sb.appendLine("Subtotal       $${"%.2f".format(subtotal)}")
    sb.appendLine("Tax (15%)      $${"%.2f".format(tax)}")
    sb.appendLine("--------------------------")
    sb.appendLine("TOTAL         $${"%.2f".format(total)}")
    sb.appendLine("--------------------------")
    sb.appendLine("Thank you for your purchase!")

    return sb.toString()
}
