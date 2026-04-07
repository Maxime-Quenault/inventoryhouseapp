package com.example.inventoryhouse.ui.screen.scanner

data class ScannerState(
    val barcode: String = "",
    val isManualMode: Boolean = false,
    val expirationDate: String = "",
    val quantity: Int = 1,
    val category: String = "Produits laitiers",
    val productName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val canSearch: Boolean
        get() = barcode.length >= 8 && !isLoading

    val canAdd: Boolean
        get() = productName.isNotBlank() && expirationDate.isNotBlank() && !isLoading
}
