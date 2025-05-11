package com.example.billprintapp.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billprintapp.models.Customer

@Composable
fun DropdownMenuDemo(selected: Customer, onSelected: (Customer) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Text(
            text = selected.name,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(16.dp)
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(
                Customer("Alice"),
                Customer("Bob"),
                Customer("Charlie")
            ).forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}
