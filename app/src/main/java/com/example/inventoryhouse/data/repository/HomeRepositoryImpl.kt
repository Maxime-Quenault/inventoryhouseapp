package com.example.inventoryhouse.data.repository

import com.example.inventoryhouse.domain.model.QuickConsumeProduct
import com.example.inventoryhouse.domain.model.StockMovement
import com.example.inventoryhouse.domain.repository.HomeRepository

class HomeRepositoryImpl : HomeRepository {
    override suspend fun fetchUsername(): String = "Maison"
    override suspend fun fetchTotalArticles(): Int = 0
    override suspend fun fetchTotalCategories(): Int = 0
    override suspend fun fetchGlobalStockHealth(): Int = 0
    override suspend fun fetchQuickConsumeItems(): List<QuickConsumeProduct> = emptyList()
    override suspend fun fetchRecentMovements(): List<StockMovement> = emptyList()
}
