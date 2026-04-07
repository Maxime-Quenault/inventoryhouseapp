package com.example.inventoryhouse.ui.screen.scanner

import com.example.inventoryhouse.data.enums.Location

sealed interface ScannerEvent {
    data class BarcodeChanged(val barcode: String) : ScannerEvent
    data class ExpirationDateChanged(val value: String) : ScannerEvent
    data class LocationChanged(val location: Location) : ScannerEvent
    data object SearchByBarcode : ScannerEvent
    data object AddProduct : ScannerEvent
    data object ClearFeedback : ScannerEvent
}
