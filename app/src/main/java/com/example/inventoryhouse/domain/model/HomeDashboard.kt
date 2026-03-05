package com.example.inventoryhouse.domain.model

data class QuickConsumeProduct(
    val name: String,
    val expiresInLabel: String
)

data class StockMovement(
    val name: String,
    val quantity: Int,
    val note: String,
    val delta: String
)
