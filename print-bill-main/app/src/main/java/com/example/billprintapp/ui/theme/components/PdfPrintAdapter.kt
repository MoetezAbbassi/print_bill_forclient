package com.example.billprintapp.ui.theme.components

import android.content.Context
import android.os.ParcelFileDescriptor
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PdfPrintAdapter(private val context: Context, private val file: File) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: android.print.PrintAttributes?,
        newAttributes: android.print.PrintAttributes?,
        cancellationSignal: android.os.CancellationSignal?,
        layoutResultCallback: LayoutResultCallback?,
        extras: android.os.Bundle?
    ) {
        layoutResultCallback?.onLayoutFinished(
            PrintDocumentInfo.Builder(file.name)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .build(), true
        )
    }

    override fun onWrite(
        pages: Array<out android.print.PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: android.os.CancellationSignal?,
        writeResultCallback: WriteResultCallback?
    ) {
        try {
            val input = FileInputStream(file)
            val output = FileOutputStream(destination?.fileDescriptor)
            input.copyTo(output)
            writeResultCallback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
        } catch (e: Exception) {
            Log.e("PdfPrintAdapter", "Print error", e)
        }
    }
}
