package com.idrisov.scanner.presentation

import android.graphics.Bitmap
import android.widget.Toast
import com.idrisov.scanner.R
import com.idrisov.scanner.exceptions.MissingSquareException

class ScannerActivity : BaseScannerActivity() {
    override fun onError(throwable: Throwable) {
        when (throwable) {
            is MissingSquareException -> Toast.makeText(
                this,
                R.string.null_corners, Toast.LENGTH_LONG
            )
                .show()
            else -> Toast.makeText(this, throwable.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDocumentAccepted(bitmap: Bitmap) {
    }

    override fun onClose() {
        finish()
    }
}
