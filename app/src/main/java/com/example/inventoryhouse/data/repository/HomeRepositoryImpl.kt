package com.example.inventoryhouse.data.repository

import com.example.inventoryhouse.domain.model.QuickConsumeProduct
import com.example.inventoryhouse.domain.model.StockMovement
import com.example.inventoryhouse.domain.repository.HomeRepository

class HomeRepositoryImpl : HomeRepository {

    override suspend fun fetchUsername(): String {
        // TODO Brancher le backend ici.
        return "Thomas"
    }

    override suspend fun fetchTotalArticles(): Int {
        // TODO Brancher le backend ici.
        return 124
    }

    override suspend fun fetchTotalCategories(): Int {
        // TODO Brancher le backend ici.
        return 8
    }

    override suspend fun fetchGlobalStockHealth(): Int {
        // TODO Brancher le backend ici.
        return 85
    }

    override suspend fun fetchQuickConsumeItems(): List<QuickConsumeProduct> {
        // TODO Brancher le backend ici.
        return listOf(
            QuickConsumeProduct(name = "Lait Entier", expiresInLabel = "EXPIRE AUJOURD'HUI"),
            QuickConsumeProduct(name = "Salade Mixte", expiresInLabel = "DANS 2 JOURS"),
            QuickConsumeProduct(name = "Poulet", expiresInLabel = "DANS 3 JOURS")
        )
    }

    override suspend fun fetchRecentMovements(): List<StockMovement> {
        // TODO Brancher le backend ici.
        return listOf(
            StockMovement(name = "Pâtes Fusilli", quantity = 3, note = "Ajouté par Thomas • Aujourd'hui, 10:30", delta = "+3"),
            StockMovement(name = "Yaourt Nature", quantity = 2, note = "Consommé par Marie • Hier, 19:45", delta = "-2"),
            StockMovement(name = "Jus d'Orange", quantity = 1, note = "Ajouté par Thomas • Hier, 14:20", delta = "+1")
        )
    }
}
