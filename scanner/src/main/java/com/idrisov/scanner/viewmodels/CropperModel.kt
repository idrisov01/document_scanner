package com.idrisov.scanner.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.idrisov.scanner.data.Corners
import com.idrisov.scanner.data.CornersFactory
import com.idrisov.scanner.domain.FindPaperSheetContours
import com.idrisov.scanner.domain.PerspectiveTransform
import com.idrisov.scanner.domain.UriToBitmap
import com.idrisov.scanner.support.Failure

class CropperModel : ViewModel() {
    private val perspectiveTransform: PerspectiveTransform = PerspectiveTransform()
    private val findPaperSheetUseCase: FindPaperSheetContours = FindPaperSheetContours()
    private val uriToBitmap: UriToBitmap = UriToBitmap()

    val corners = MutableLiveData<Corners>()
    val originalBitmap = MutableLiveData<Bitmap>()
    val bitmapToCrop = MutableLiveData<Bitmap>()
    val errors = MutableLiveData<Throwable>()

    fun onViewCreated(uri: Uri, screenOrientationDeg: Int, defaultCorners: Corners) {
        viewModelScope.launch {
            uriToBitmap(
                UriToBitmap.Params(
                    uri = uri,
                    screenOrientationDeg = screenOrientationDeg
                )
            ) { either ->
                either.fold(::handleFailure) { preview ->
                    originalBitmap.value = preview
                    analyze(preview, defaultCorners)
                }
            }
        }
    }

    fun onCornersAccepted(bitmap: Bitmap) {
        corners.value?.let { acceptedCorners ->
            val acceptedAndOrderedCorners = listOf(
                acceptedCorners.topLeft,
                acceptedCorners.topRight,
                acceptedCorners.bottomRight,
                acceptedCorners.bottomLeft
            )

            viewModelScope.launch {
                perspectiveTransform(
                    PerspectiveTransform.Params(
                        bitmap = bitmap,
                        corners = CornersFactory.create(
                            acceptedAndOrderedCorners,
                            acceptedCorners.size
                        )
                    )
                ) { bitmap ->
                    bitmapToCrop.value = bitmap
                }
            }
        }
    }

    private fun analyze(bitmap: Bitmap, defaultCorners: Corners) {
        viewModelScope.launch {
            findPaperSheetUseCase(
                FindPaperSheetContours.Params(bitmap)
            ) { foundCorners: Corners? ->
                corners.value = foundCorners ?: defaultCorners
            }
        }
    }

    private fun handleFailure(failure: Failure) {
        errors.value = failure.origin
    }
}
