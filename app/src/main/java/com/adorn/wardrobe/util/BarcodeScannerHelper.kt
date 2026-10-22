package com.adorn.wardrobe.util

import android.app.Activity
import android.widget.Toast
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

object BarcodeScannerHelper {

    fun scan(
        activity: Activity,
        onCodeScanned: (code: String, isUrl: Boolean) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        try {
            val options = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_DATA_MATRIX
                )
                .enableAutoZoom()
                .build()

            val scanner = GmsBarcodeScanning.getClient(activity, options)
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val raw = barcode.rawValue?.trim() ?: ""
                    if (raw.isNotBlank()) {
                        val isUrl = raw.startsWith("http://", ignoreCase = true) ||
                                raw.startsWith("https://", ignoreCase = true)
                        onCodeScanned(raw, isUrl)
                    } else {
                        Toast.makeText(activity, "No barcode content detected", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnCanceledListener {
                    // User dismissed scanner
                }
                .addOnFailureListener { e ->
                    onError(e.localizedMessage ?: "Barcode scanning unavailable")
                }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Scanner could not be initialized")
        }
    }
}
