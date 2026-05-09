package com.example.inventoryhouse.data.repository

import com.example.inventoryhouse.data.enums.Location
import com.example.inventoryhouse.data.model.Product
import com.example.inventoryhouse.data.remote.dto.ItemDto
import com.example.inventoryhouse.data.remote.dto.ReferenceDto
import com.example.inventoryhouse.data.remote.dto.ReferencesResponseDto
import com.example.inventoryhouse.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

class RemoteProductRepository(
    private val inventoryRepository: InventoryRepository,
    private val houseId: Long
) : ProductRepository {

    private val products = MutableStateFlow<List<Product>>(emptyList())
    private var references: ReferencesResponseDto? = null

    override fun getProductsStream(): Flow<List<Product>> = products

    override suspend fun refresh() {
        products.value = inventoryRepository.getItems(houseId).map { it.toProduct() }
    }

    override suspend fun addProduct(product: Product) {
        createProduct(product)
        refresh()
    }

    override suspend fun addProducts(products: List<Product>) {
        products.forEach { createProduct(it) }
        refresh()
    }

    private suspend fun createProduct(product: Product) {
        val refs = getReferences()
        val location = refs.locations.matchLocation(product.location)
        val category = refs.stockCategories.firstOrNull()
            ?: throw RuntimeException("Aucune categorie de stock disponible.")

        inventoryRepository.createItem(
            houseId = houseId,
            category = category,
            location = location,
            name = product.name,
            quantity = product.quantity,
            unit = product.quantityUnit.ifBlank { "piece" },
            expirationDate = product.expiredDate
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toString()
        )
    }

    override suspend fun removeProduct(product: Product) {
        inventoryRepository.deleteItem(houseId = houseId, itemId = product.id)
        refresh()
    }

    private suspend fun getReferences(): ReferencesResponseDto {
        val current = references
        if (current != null) return current
        return inventoryRepository.getReferences().also { references = it }
    }

    private fun List<ReferenceDto>.matchLocation(location: Location): ReferenceDto {
        val keywords = when (location) {
            Location.FRESH -> listOf("frigo", "frais", "fresh", "refriger")
            Location.FROZEN -> listOf("congel", "frozen")
            Location.DRY -> listOf("placard", "sec", "dry")
        }
        return firstOrNull { reference ->
            val name = reference.name.lowercase()
            keywords.any { keyword -> name.contains(keyword) }
        } ?: firstOrNull() ?: throw RuntimeException("Aucun emplacement de stock disponible.")
    }

    private fun ItemDto.toProduct(): Product {
        return Product(
            id = id,
            name = name,
            expiredDate = expirationDate.toLocalDateOrDefault(),
            location = location?.name.toLocation(),
            imageUrl = "",
            quantity = quantity,
            quantityUnit = unit
        )
    }

    private fun String?.toLocalDateOrDefault(): LocalDate {
        if (isNullOrBlank()) return LocalDate.now().plusYears(1)
        return try {
            OffsetDateTime.parse(this).toLocalDate()
        } catch (_: Exception) {
            runCatching { LocalDate.parse(this.take(10)) }.getOrElse { LocalDate.now().plusYears(1) }
        }
    }

    private fun String?.toLocation(): Location {
        val value = this?.lowercase().orEmpty()
        return when {
            value.contains("congel") || value.contains("frozen") -> Location.FROZEN
            value.contains("frigo") || value.contains("frais") || value.contains("fresh") -> Location.FRESH
            else -> Location.DRY
        }
    }
}
