package com.example.inventoryhouse.data.repository

import com.example.inventoryhouse.data.dao.ProductDao
import com.example.inventoryhouse.data.model.Product
import com.example.inventoryhouse.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class InMemoryProductRepository(
    private val dao: ProductDao
) : ProductRepository {

    override fun getProductsStream(): Flow<List<Product>> = dao.getAll()

    override suspend fun addProduct(product: Product) {
        dao.insert(product)
    }

    override suspend fun removeProduct(product: Product) {
        dao.delete(product)
    }
}