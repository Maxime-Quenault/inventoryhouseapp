package com.example.inventoryhouse.ui.screen.scanner

import android.net.Uri
import com.example.inventoryhouse.data.enums.Location

sealed interface ScannerEvent {
    data class BarcodeDetected(val barcode: String) : ScannerEvent
    data class ReceiptImageCaptured(val uri: Uri) : ScannerEvent
    data class ReceiptPdfSelected(val uri: Uri) : ScannerEvent
    data object ReceiptCapturePermissionDenied : ScannerEvent
    data class ReceiptCaptureFailed(val message: String) : ScannerEvent
    data class ReceiptDraftNameChanged(val id: Long, val value: String) : ScannerEvent
    data class ReceiptDraftPackageFormatChanged(val id: Long, val value: String) : ScannerEvent
    data class ReceiptDraftSelectedChanged(val id: Long, val selected: Boolean) : ScannerEvent
    data class ReceiptDraftIncreaseCount(val id: Long) : ScannerEvent
    data class ReceiptDraftDecreaseCount(val id: Long) : ScannerEvent
    data class RemoveReceiptDraft(val id: Long) : ScannerEvent
    data class ReceiptLocationChanged(val location: Location) : ScannerEvent
    data class ReceiptExpirationDateChanged(val value: String) : ScannerEvent
    data class ProductNameChanged(val value: String) : ScannerEvent
    data class QuantityUnitChanged(val value: String) : ScannerEvent
    data class LocationChanged(val location: Location) : ScannerEvent
    data object IncreaseQuantity : ScannerEvent
    data object DecreaseQuantity : ScannerEvent
    data class ExpirationDateChanged(val value: String) : ScannerEvent
    data object ShowAddForm : ScannerEvent
    data object HideAddForm : ScannerEvent
    data object AddProduct : ScannerEvent
    data object AddReceiptProducts : ScannerEvent
    data object ClearReceiptDrafts : ScannerEvent
    data object ClearFeedback : ScannerEvent
}
