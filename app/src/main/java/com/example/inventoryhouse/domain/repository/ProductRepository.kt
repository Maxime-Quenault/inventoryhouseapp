package com.example.inventoryhouse.domain.repository

import com.example.inventoryhouse.data.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProductsStream(): Flow<List<Product>>
    suspend fun refresh()
    suspend fun addProduct(product: Product)
    suspend fun addProducts(products: List<Product>) {
        products.forEach { addProduct(it) }
    }
    suspend fun removeProduct(product: Product)
}
