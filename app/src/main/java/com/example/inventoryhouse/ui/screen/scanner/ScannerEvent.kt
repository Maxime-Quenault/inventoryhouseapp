package com.example.inventoryhouse.ui.screen.scanner

import com.example.inventoryhouse.data.enums.Location

sealed interface ScannerEvent {
    data class BarcodeDetected(val barcode: String) : ScannerEvent
    data class ProductNameChanged(val value: String) : ScannerEvent
    data class LocationChanged(val location: Location) : ScannerEvent
    data object IncreaseQuantity : ScannerEvent
    data object DecreaseQuantity : ScannerEvent
    data class ExpirationDateChanged(val value: String) : ScannerEvent
    data object ShowAddForm : ScannerEvent
    data object HideAddForm : ScannerEvent
    data object AddProduct : ScannerEvent
    data object ClearFeedback : ScannerEvent
}
