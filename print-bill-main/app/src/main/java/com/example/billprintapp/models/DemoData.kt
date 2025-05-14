// models/DemoData.kt
package com.example.billprintapp.models

object DemoData {
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
}