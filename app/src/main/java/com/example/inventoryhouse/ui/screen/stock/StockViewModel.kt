package com.example.inventoryhouse.ui.screen.stock

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.inventoryhouse.data.model.Product
import com.example.inventoryhouse.domain.repository.ProductRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class StockViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _state = mutableStateOf(StockState())
    val state: State<StockState> = _state

    init {
        repository.getProductsStream()
            .onEach { products ->
                _state.value = _state.value.copy(products = products)
            }
            .launchIn(viewModelScope)
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.addProduct(product)
        }
    }

    fun removeProduct(product: Product) {
        viewModelScope.launch {
            repository.removeProduct(product)
        }
    }

    companion object {
        fun provideFactory(repository: ProductRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StockViewModel(repository)
            }
        }
    }
}