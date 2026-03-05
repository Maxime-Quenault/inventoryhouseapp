package com.example.inventoryhouse.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.inventoryhouse.domain.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.RefreshData -> refreshData()
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    username = "Bonjour, ${repository.fetchUsername()}",
                    totalArticles = repository.fetchTotalArticles(),
                    totalCategories = repository.fetchTotalCategories(),
                    globalStockHealth = repository.fetchGlobalStockHealth(),
                    quickConsumeItems = repository.fetchQuickConsumeItems().map { item ->
                        QuickConsumeItem(name = item.name, expiresInLabel = item.expiresInLabel)
                    },
                    recentMovements = repository.fetchRecentMovements().map { movement ->
                        RecentMovement(
                            name = movement.name,
                            quantity = movement.quantity,
                            note = movement.note,
                            delta = movement.delta
                        )
                    }
                )
            }
        }
    }

    companion object {
        fun provideFactory(repository: HomeRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(repository)
            }
        }
    }
}
