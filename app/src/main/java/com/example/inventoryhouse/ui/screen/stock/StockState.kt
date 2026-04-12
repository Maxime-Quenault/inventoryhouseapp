package com.example.inventoryhouse.ui.screen.stock

import com.example.inventoryhouse.data.enums.Location
import com.example.inventoryhouse.data.model.Product
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class ExpirationTone {
    SAFE,
    WARNING,
    DANGER
}

data class StockItemUi(
    val id: Long,
    val name: String,
    val details: String,
    val category: Location,
    val expirationLabel: String,
    val expirationTone: ExpirationTone,
    val sourceProduct: Product? = null
)

data class StockState(
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: Location? = null
) {
    /**
     * Point d'entrée unique pour afficher les aliments.
     * On peut remplacer [sampleRawItems] plus tard par une API sans toucher l'UI.
     */
    val displayItems: List<StockItemUi>
        get() {
            val mappedProducts = products.map { it.toUiModel() }
            val baseItems = if (mappedProducts.isEmpty()) sampleRawItems else mappedProducts
            return baseItems
                .filter { item ->
                    selectedCategory == null || item.category == selectedCategory
                }
                .filter { item ->
                    searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
                }
        }

    companion object {
        val sampleRawItems = listOf(
            StockItemUi(1, "Yaourt Nature", "4 unités • Frais", Location.FRESH, "EXPIRE DANS 2 JOURS", ExpirationTone.DANGER),
            StockItemUi(2, "Pâtes Penne", "500g • Sec", Location.DRY, "EXPIRE EN 2025", ExpirationTone.SAFE),
            StockItemUi(3, "Lait Entier", "1L • Frais", Location.FRESH, "EXPIRE DANS 5 JOURS", ExpirationTone.WARNING),
            StockItemUi(4, "Petits Pois", "1kg • Congelé", Location.FROZEN, "EXPIRÉ LE 12/05", ExpirationTone.DANGER)
        )
    }
}

private fun Product.toUiModel(): StockItemUi {
    val today = LocalDate.now()
    val daysToExpire = ChronoUnit.DAYS.between(today, expiredDate).toInt()
    val expirationTone = when {
        daysToExpire < 0 -> ExpirationTone.DANGER
        daysToExpire <= 2 -> ExpirationTone.DANGER
        daysToExpire <= 7 -> ExpirationTone.WARNING
        else -> ExpirationTone.SAFE
    }

    val expirationLabel = when {
        daysToExpire < 0 -> "EXPIRÉ"
        daysToExpire == 0 -> "EXPIRE AUJOURD'HUI"
        daysToExpire <= 7 -> "EXPIRE DANS $daysToExpire JOURS"
        else -> "EXPIRE LE ${expiredDate}".replace('-', '/')
    }

    return StockItemUi(
        id = id,
        name = name,
        details = "Stock • ${location.name}",
        category = location,
        expirationLabel = expirationLabel,
        expirationTone = expirationTone,
        sourceProduct = this
    )
}
