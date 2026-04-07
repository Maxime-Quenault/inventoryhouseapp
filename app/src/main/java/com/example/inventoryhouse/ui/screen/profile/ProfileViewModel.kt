package com.example.inventoryhouse.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.inventoryhouse.data.enums.Location
import com.example.inventoryhouse.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class ProfileViewModel(
    repository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        repository.getProductsStream()
            .onEach { products ->
                _state.update { currentState ->
                    currentState.copy(
                        totalItems = products.size,
                        expiringSoonCount = products.count { product ->
                            !product.expiredDate.isBefore(LocalDate.now()) &&
                                product.expiredDate.isBefore(LocalDate.now().plusDays(4))
                        },
                        locations = Location.entries.map { location ->
                            LocationStockSummary(
                                location = location,
                                count = products.count { it.location == location }
                            )
                        }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.EditHousehold -> Unit
            ProfileEvent.InviteMember -> Unit
        }
    }

    companion object {
        fun provideFactory(repository: ProductRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProfileViewModel(repository)
            }
        }
    }
}
