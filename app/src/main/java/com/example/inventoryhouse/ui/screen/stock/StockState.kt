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
    val selectedCategory: Location? = null,
    val errorMessage: String? = null
) {
    val displayItems: List<StockItemUi>
        get() {
            return products
                .map { it.toUiModel() }
                .filter { item ->
                    selectedCategory == null || item.category == selectedCategory
                }
                .filter { item ->
                    searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
                }
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
        daysToExpire < 0 -> "EXPIRE"
        daysToExpire == 0 -> "EXPIRE AUJOURD'HUI"
        daysToExpire <= 7 -> "EXPIRE DANS $daysToExpire JOURS"
        else -> "EXPIRE LE ${expiredDate}".replace('-', '/')
    }

    return StockItemUi(
        id = id,
        name = name,
        details = "${quantity} ${quantityUnit} - ${location.name}",
        category = location,
        expirationLabel = expirationLabel,
        expirationTone = expirationTone,
        sourceProduct = this
    )
}
