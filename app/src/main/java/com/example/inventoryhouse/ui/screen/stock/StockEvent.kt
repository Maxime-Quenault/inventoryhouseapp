package com.example.inventoryhouse.ui.screen.stock

import com.example.inventoryhouse.data.model.Product

sealed interface StockEvent {
    data class SearchChanged(val query: String) : StockEvent
    data class SelectCategory(val category: StockCategory?) : StockEvent
    data class RemoveProduct(val product: Product) : StockEvent
}
