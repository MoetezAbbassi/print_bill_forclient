package com.example.billprintapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.billprintapp.models.Customer
import com.example.billprintapp.models.Item
import com.example.billprintapp.ui.theme.components.DropdownMenuDemo
import java.text.SimpleDateFormat
import java.util.*

val customers = listOf(Customer("Alice"), Customer("Bob"), Customer("Charlie"))
val items = listOf(
    Item("Item A", 10.0),
    Item("Item B", 15.0),
    Item("Item C", 8.5)
)

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
            kotlinx.coroutines.delay(1500)
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
