package com.example.inventoryhouse.ui.screen.scanner.manualadd

import com.example.inventoryhouse.data.enums.Location

data class AddProductState(
    val name: String = "",
    val expirationDate: String = "",
    val location: Location = Location.FRIDGE,
    val isValid: Boolean = false,
    val errorMessage: String? = null
)
