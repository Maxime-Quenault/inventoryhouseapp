package com.example.inventoryhouse.ui.screen.scanner.manualadd

import androidx.lifecycle.ViewModel
import com.example.inventoryhouse.data.enums.Location
import com.example.inventoryhouse.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeParseException

class AddProductViewModel : ViewModel() {
    private val _state = MutableStateFlow(AddProductState())
    val state: StateFlow<AddProductState> = _state.asStateFlow()

    fun onNameChange(newName: String) {
        _state.update { it.copy(name = newName) }
        validate()
    }

    fun onDateChange(newDate: String) {
        _state.update { it.copy(expirationDate = newDate) }
        validate()
    }

    fun onLocationChange(newLocation: Location) {
        _state.update { it.copy(location = newLocation) }
    }

    private fun validate() {
        val isNameValid = _state.value.name.isNotBlank()
        val isDateValid = try {
            LocalDate.parse(_state.value.expirationDate)
            true
        } catch (_: DateTimeParseException) {
            false
        }

        _state.update {
            it.copy(
                isValid = isNameValid && isDateValid,
                errorMessage = if (it.expirationDate.isNotBlank() && !isDateValid) {
                    "Date invalide (AAAA-MM-JJ)"
                } else {
                    null
                }
            )
        }
    }

    fun getProduct(): Product? {
        val currentState = _state.value
        return if (currentState.isValid) {
            Product(
                id = System.currentTimeMillis(),
                name = currentState.name,
                expiredDate = LocalDate.parse(currentState.expirationDate),
                location = currentState.location
            )
        } else {
            null
        }
    }
}
