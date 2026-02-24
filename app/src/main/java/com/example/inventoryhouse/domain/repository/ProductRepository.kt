package com.example.inventoryhouse.domain.repository

import com.example.inventoryhouse.data.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProductsStream(): Flow<List<Product>>
    suspend fun addProduct(product: Product)
    suspend fun removeProduct(product: Product)
}