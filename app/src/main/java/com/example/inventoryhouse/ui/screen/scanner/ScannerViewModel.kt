package com.example.inventoryhouse.ui.screen.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.inventoryhouse.data.enums.Location
import com.example.inventoryhouse.data.model.Product
import com.example.inventoryhouse.data.remote.api.OpenFoodFactsApi
import com.example.inventoryhouse.data.remote.dto.OpenFoodFactsProductDto
import com.example.inventoryhouse.data.remote.dto.OpenFoodFactsProductResponseDto
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
            is ScannerEvent.BarcodeDetected -> onBarcodeDetected(event.barcode)

            is ScannerEvent.ProductNameChanged -> _state.update {
                it.copy(productName = event.value, errorMessage = null)
            }

            is ScannerEvent.LocationChanged -> {
                _state.update { it.copy(location = event.location) }
            }

            is ScannerEvent.ExpirationDateChanged -> _state.update {
                it.copy(expirationDate = event.value, errorMessage = null)
            }

            ScannerEvent.IncreaseQuantity -> _state.update { it.copy(quantity = it.quantity + 1) }
            ScannerEvent.DecreaseQuantity -> _state.update { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }
            ScannerEvent.ShowAddForm -> _state.update {
                it.copy(isAddFormVisible = true, errorMessage = null, successMessage = null)
            }

            ScannerEvent.HideAddForm -> _state.update {
                it.copy(isAddFormVisible = false, errorMessage = null, successMessage = null)
            }

            ScannerEvent.ClearFeedback -> _state.update {
                it.copy(errorMessage = null, successMessage = null)
            }

            ScannerEvent.AddProduct -> addProduct()
        }
    }

    private fun onBarcodeDetected(rawBarcode: String) {
        val barcode = rawBarcode.filter(Char::isDigit).take(14)
        if (barcode.length < 8) return

        val current = state.value
        if (current.barcode == barcode && current.hasDetectedBarcode) return

        _state.update {
            it.copy(
                barcode = barcode,
                hasDetectedBarcode = true,
                isAddFormVisible = true,
                errorMessage = null,
                successMessage = "Code-barres détecté : $barcode"
            )
        }

        viewModelScope.launch {
            try {
                val resDto: OpenFoodFactsProductDto? =
                    openFoodFactsApi.getProductByBarcode(barcode).product

                if (resDto == null) {
                    _state.update {
                        it.copy(errorMessage = "Aucun produit trouvé")
                    }
                    return@launch
                }

                _state.update {
                    it.copy(
                        productName = resDto.productName.orEmpty(),
                        quantity = resDto.quantity.orEmpty(),
                        errorMessage = null,
                        successMessage = "Produit trouvé : ${resDto.productName.orEmpty()}"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = "Erreur lors de l'appel API : ${e.message}")
                }
            }
        }
    }

    private fun Int?.orEmpty(): Int {
        return 0;
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
            _state.update { it.copy(errorMessage = "Nom du produit obligatoire") }
            return
        }

        viewModelScope.launch {
            productRepository.addProduct(
                Product(
                    id = System.currentTimeMillis(),
                    name = current.productName,
                    expiredDate = expirationDate,
                    location = current.location,
                    imageUrl = current.imageUrl,
                    quantity = current.quantity,
                    quantityUnit = current.quantityUnit
                )
            )

            _state.update {
                it.copy(
                    barcode = "",
                    productName = "",
                    expirationDate = "",
                    quantity = 1,
                    hasDetectedBarcode = false,
                    isAddFormVisible = false,
                    errorMessage = null,
                    successMessage = "Produit enregistré"
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
