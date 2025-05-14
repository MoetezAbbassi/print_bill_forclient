package com.example.billprintapp.utils

import com.example.billprintapp.models.EditableItem
import java.text.SimpleDateFormat
import java.util.*

object ReceiptBuilder {
    fun createFancyReceiptText(customerName: String, items: List<EditableItem>): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault())
        val dateStr = sdf.format(Date())

        val sb = StringBuilder()
        sb.appendLine("<C><B>-------------------------------</B></C>")
        sb.appendLine("<C><B>    ⭐ FSLQD ICE CREAM CO. ⭐    </B></C>")
        sb.appendLine("<C><B>-------------------------------</B></C>")
        sb.appendLine("Date: $dateStr")
        sb.appendLine("Customer: $customerName")
        sb.appendLine("--------------------------------")
        sb.appendLine(String.format("%-18s %5s %7s", "Item", "Qty", "Total"))
        sb.appendLine("--------------------------------")

        var subtotal = 0.0
        for (item in items) {
            if (item.name.isNotBlank() && item.quantity > 0) {
                val total = item.price * item.quantity
                sb.appendLine(String.format("%-18s %5d %7.2f", item.name, item.quantity, total))
                subtotal += total
            }
        }

        val tax = subtotal * 0.15
        val total = subtotal + tax

        sb.appendLine("--------------------------------")
        sb.appendLine(String.format("%-20s %10.2f", "Subtotal:", subtotal))
        sb.appendLine(String.format("%-20s %10.2f", "Tax (15%):", tax))
        sb.appendLine("--------------------------------")
        sb.appendLine(String.format("<B>%-20s $%10.2f</B>", "TOTAL:", total))
        sb.appendLine("--------------------------------")
        sb.appendLine("<C>Thank you for your purchase!</C>")
        sb.appendLine("<C>Please come again soon.</C>")
        sb.appendLine("<C><QR>https://example.com/receipt?id=123456</QR></C>")
        sb.appendLine("<C><CUT></C>")

        return sb.toString()
    }
}
