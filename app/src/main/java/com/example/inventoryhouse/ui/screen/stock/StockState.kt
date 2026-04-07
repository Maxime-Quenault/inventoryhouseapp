package com.example.inventoryhouse.ui.screen.stock

import com.example.inventoryhouse.data.enums.Location
import com.example.inventoryhouse.data.model.Product

data class StockState(
    val products: List<Product> = emptyList(),
    val selectedLocation: Location? = null
) {
    val filteredProducts: List<Product>
        get() = selectedLocation?.let { location ->
            products.filter { it.location == location }
        } ?: products
}
