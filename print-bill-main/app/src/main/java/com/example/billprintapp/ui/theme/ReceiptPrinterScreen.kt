package com.example.billprintapp.ui

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.billprintapp.utils.ReceiptBitmapBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ReceiptPrinterScreen(
    onSendBitmap: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("receipt_prefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var selectedCustomerName by remember { mutableStateOf("") }
    var customerVatMap by remember {
        mutableStateOf(prefs.getStringSet("customers", setOf())!!
            .mapNotNull {
                val split = it.split("|||")
                if (split.size == 2) split[0] to split[1] else null
            }.toMap().toMutableMap())
    }

    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerVat by remember { mutableStateOf("") }

    val productOptions = listOf("Ice 4kg", "Ice 2kg", "ICE Cup", "Other")
    var selectedProduct by remember { mutableStateOf(productOptions[0]) }
    var customProductName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("3.5") }
    var quantity by remember { mutableStateOf("1") }

    val items = remember { mutableStateListOf<EditableItem>() }
    var bitmapPreview by remember { mutableStateOf<Bitmap?>(null) }
    var showPreview by remember { mutableStateOf(false) }

    LaunchedEffect(selectedProduct) {
        price = when (selectedProduct) {
            "Ice 4kg" -> "3.5"
            "Ice 2kg" -> "2.0"
            "ICE Cup" -> "1.0"
            else -> price
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Customer Information", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            DropdownMenuWithTextField(
                options = customerVatMap.keys.toList(),
                selectedValue = selectedCustomerName,
                onValueChange = { selectedCustomerName = it },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    if (selectedCustomerName.isNotBlank()) {
                        customerVatMap.remove(selectedCustomerName)
                        val set = customerVatMap.map { "${it.key}|||${it.value}" }.toSet()
                        prefs.edit().putStringSet("customers", set).apply()
                        selectedCustomerName = ""
                    }
                },
                modifier = Modifier.height(55.dp)
            ) {
                Text("🗑️")
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newCustomerName,
                onValueChange = { newCustomerName = it },
                label = { Text("Name") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = newCustomerVat,
                onValueChange = { newCustomerVat = it },
                label = { Text("VAT ID") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(6.dp))
        Button(onClick = {
            if (newCustomerName.isNotBlank() && newCustomerVat.isNotBlank()) {
                if (!newCustomerVat.all { it.isDigit() }) {
                    Toast.makeText(context, "❗ VAT ID must be numeric only", Toast.LENGTH_LONG).show()
                } else {
                    customerVatMap[newCustomerName] = newCustomerVat
                    val set = customerVatMap.map { "${it.key}|||${it.value}" }.toSet()
                    prefs.edit().putStringSet("customers", set).apply()
                    selectedCustomerName = newCustomerName
                    newCustomerName = ""
                    newCustomerVat = ""
                }
            }
        }, modifier = Modifier.align(Alignment.End)) {
            Text("➕ Add Customer")
        }

        Spacer(Modifier.height(20.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        Text("Items in Receipt", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DropdownMenuWithTextField(
                options = productOptions,
                selectedValue = selectedProduct,
                onValueChange = { selectedProduct = it },
                modifier = Modifier.weight(1f)
            )

            if (selectedProduct == "Other") {
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = customProductName,
                    onValueChange = { customProductName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(90.dp)
            )

            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Qty") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(70.dp)
            )
        }

        Spacer(Modifier.height(10.dp))
        Button(onClick = {
            val name = if (selectedProduct == "Other") customProductName else selectedProduct
            val item = EditableItem(name, price.toDoubleOrNull() ?: 0.0, quantity.toIntOrNull() ?: 0)
            if (name.isNotBlank()) items.add(item)
            selectedProduct = productOptions[0]
            price = "3.5"
            quantity = "1"
        }) {
            Text("➕ Add Item to Receipt")
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.fillMaxHeight(0.35f)) {
            items(items.size) { index ->
                val item = items[index]
                Text("- ${item.name} (${item.quantity} x ${item.price})")
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                items.clear()
                selectedProduct = productOptions[0]
                price = "3.5"
                quantity = "1"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Items")
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                if (selectedCustomerName.isBlank() || items.isEmpty()) return@Button
                val vatId = customerVatMap[selectedCustomerName] ?: ""
                val bmp = ReceiptBitmapBuilder.buildReceiptBitmap(
                    customerName = selectedCustomerName,
                    vatId = vatId,
                    items = items
                )
                bitmapPreview = bmp
                showPreview = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Preview & Print")
        }
    }

    if (showPreview && bitmapPreview != null) {
        AlertDialog(
            onDismissRequest = { showPreview = false },
            confirmButton = {
                TextButton(onClick = {
                    showPreview = false
                    bitmapPreview?.let {
                        onSendBitmap(it)
                        scope.launch(Dispatchers.IO) {
                            val dao = ReceiptDatabase.getInstance(context).receiptDao()
                            val total = items.sumOf { it.price * it.quantity }
                            val entity = ReceiptEntity(
                                customerName = selectedCustomerName,
                                receiptText = "Receipt Printed",
                                total = total,
                                timestamp = System.currentTimeMillis()
                            )
                            dao.insert(entity)
                        }
                        items.clear()
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
                    bitmap = bitmapPreview!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp, max = 600.dp)
                )
            }
        )
    }
}

@Composable
fun DropdownMenuWithTextField(
    options: List<String>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.padding(end = 8.dp)) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            label = { Text("Select") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
