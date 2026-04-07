package com.example.inventoryhouse.ui.screen.scanner

import com.example.inventoryhouse.data.enums.Location

data class ScannerState(
    val barcode: String = "",
    val selectedLocation: Location = Location.FRIDGE,
    val expirationDate: String = "",
    val isLoading: Boolean = false,
    val productName: String = "",
    val brand: String = "",
    val imageUrl: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val canSearch: Boolean
        get() = barcode.length >= 8 && !isLoading

    val canAdd: Boolean
        get() = productName.isNotBlank() && expirationDate.isNotBlank() && !isLoading
}
