package com.example.inventoryhouse.ui.screen.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.inventoryhouse.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StockViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _state = MutableStateFlow(StockState())
    val state: StateFlow<StockState> = _state.asStateFlow()

    init {
        repository.getProductsStream()
            .onEach { products ->
                _state.update { it.copy(products = products) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            runCatching { repository.refresh() }
                .onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message ?: "Erreur de chargement du stock") }
                }
        }
    }

    fun onEvent(event: StockEvent) {
        when (event) {
            is StockEvent.SearchChanged -> _state.update { it.copy(searchQuery = event.query) }
            is StockEvent.SelectCategory -> _state.update { it.copy(selectedCategory = event.category) }
            is StockEvent.RemoveProduct -> {
                viewModelScope.launch {
                    runCatching { repository.removeProduct(event.product) }
                        .onFailure { error ->
                            _state.update { it.copy(errorMessage = error.message ?: "Impossible de supprimer cet item") }
                        }
                }
            }
            StockEvent.Refresh -> {
                viewModelScope.launch {
                    runCatching { repository.refresh() }
                        .onFailure { error ->
                            _state.update { it.copy(errorMessage = error.message ?: "Erreur de chargement du stock") }
                        }
                }
            }
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
