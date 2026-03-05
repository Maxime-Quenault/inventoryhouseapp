package com.example.inventoryhouse.domain.repository

import com.example.inventoryhouse.domain.model.QuickConsumeProduct
import com.example.inventoryhouse.domain.model.StockMovement

interface HomeRepository {
    suspend fun fetchUsername(): String
    suspend fun fetchTotalArticles(): Int
    suspend fun fetchTotalCategories(): Int
    suspend fun fetchGlobalStockHealth(): Int
    suspend fun fetchQuickConsumeItems(): List<QuickConsumeProduct>
    suspend fun fetchRecentMovements(): List<StockMovement>
}
