package com.example.inventoryhouse.ui.screen.scanner

import com.example.inventoryhouse.data.enums.Location

data class ScannerState(
    val barcode: String = "",
    val expirationDate: String = "",
    val quantity: Int = 1,
    val quantityUnit: String = "g",
    val imageUrl: String = "",
    val location: Location = Location.DRY,
    val productName: String = "",
    val isLoading: Boolean = false,
    val isAddFormVisible: Boolean = false,
    val hasDetectedBarcode: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val canAdd: Boolean
        get() = productName.isNotBlank() && expirationDate.isNotBlank() && !isLoading
}
