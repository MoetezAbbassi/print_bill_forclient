package com.example.billprintapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.billprintapp.ui.ReceiptPrinterScreen
import com.example.billprintapp.utils.PdfGenerator
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBluetoothPermissions()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ReceiptPrinterScreen(
                        onPrintThermal = { receiptText ->
                            Toast.makeText(this, "Thermal not supported in this mode.", Toast.LENGTH_SHORT).show()
                        },
                        onPrintPdf = { /* no-op, now handled by sendPdfFile */ },
                        onSendPdfFile = { file ->
                            sendPdfToScposApp(file)
                        }
                    )
                }
            }
        }
    }

    private fun sendPdfToScposApp(pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                setPackage("com.loopedlabs.escposprintservice")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Could not open SCPOS Print Service", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                101
            )
        }
    }
}
