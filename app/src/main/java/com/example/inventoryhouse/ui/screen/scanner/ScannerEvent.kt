package com.example.inventoryhouse.ui.screen.scanner

sealed interface ScannerEvent {
    data class BarcodeChanged(val barcode: String) : ScannerEvent
    data class ProductNameChanged(val value: String) : ScannerEvent
    data class CategoryChanged(val value: String) : ScannerEvent
    data object IncreaseQuantity : ScannerEvent
    data object DecreaseQuantity : ScannerEvent
    data class ExpirationDateChanged(val value: String) : ScannerEvent
    data object ToggleManualMode : ScannerEvent
    data object SearchByBarcode : ScannerEvent
    data object AddProduct : ScannerEvent
}
