package com.example.inventoryhouse.ui.screen.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.inventoryhouse.data.model.Product
import com.example.inventoryhouse.data.remote.api.OpenFoodFactsApi
import com.example.inventoryhouse.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

class ScannerViewModel(
    private val openFoodFactsApi: OpenFoodFactsApi,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScannerState())
    val state: StateFlow<ScannerState> = _state.asStateFlow()

    fun onEvent(event: ScannerEvent) {
        when (event) {
            is ScannerEvent.BarcodeChanged -> _state.update {
                it.copy(barcode = event.barcode.filter { c -> c.isDigit() }.take(14), errorMessage = null)
            }

            is ScannerEvent.ExpirationDateChanged -> _state.update {
                it.copy(expirationDate = event.value, errorMessage = null)
            }

            is ScannerEvent.LocationChanged -> _state.update {
                it.copy(selectedLocation = event.location)
            }

            ScannerEvent.SearchByBarcode -> searchByBarcode()
            ScannerEvent.AddProduct -> addProduct()
            ScannerEvent.ClearFeedback -> _state.update { it.copy(errorMessage = null, successMessage = null) }
        }
    }

    private fun searchByBarcode() {
        val barcode = state.value.barcode
        if (barcode.length < 8) {
            _state.update { it.copy(errorMessage = "Code-barres invalide") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            runCatching { openFoodFactsApi.getProductByBarcode(barcode) }
                .onSuccess { response ->
                    val name = response.product?.productName.orEmpty()
                    if (response.status == 1 && name.isNotBlank()) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                productName = name,
                                brand = response.product?.brands.orEmpty(),
                                imageUrl = response.product?.imageUrl
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Produit introuvable pour ce code-barres"
                            )
                        }
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(isLoading = false, errorMessage = "Impossible de récupérer le produit")
                    }
                }
        }
    }

    private fun addProduct() {
        val current = state.value
        val expirationDate = try {
            LocalDate.parse(current.expirationDate)
        } catch (_: DateTimeParseException) {
            _state.update { it.copy(errorMessage = "Date invalide (format AAAA-MM-JJ)") }
            return
        }

        if (current.productName.isBlank()) {
            _state.update { it.copy(errorMessage = "Scanne d'abord un produit") }
            return
        }

        viewModelScope.launch {
            productRepository.addProduct(
                Product(
                    id = System.currentTimeMillis(),
                    name = current.productName,
                    expiredDate = expirationDate,
                    location = current.selectedLocation
                )
            )
            _state.update {
                it.copy(
                    successMessage = "Produit ajouté au stock",
                    errorMessage = null,
                    barcode = "",
                    productName = "",
                    brand = "",
                    imageUrl = null,
                    expirationDate = ""
                )
            }
        }
    }

    companion object {
        fun provideFactory(
            openFoodFactsApi: OpenFoodFactsApi,
            productRepository: ProductRepository
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ScannerViewModel(openFoodFactsApi, productRepository)
            }
        }
    }
}
