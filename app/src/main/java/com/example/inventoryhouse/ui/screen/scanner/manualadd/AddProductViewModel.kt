package com.example.inventoryhouse.ui.screen.scanner.manualadd

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.inventoryhouse.data.enums.Location
import com.example.inventoryhouse.data.model.Product
import java.time.LocalDate
import java.time.format.DateTimeParseException

class AddProductViewModel : ViewModel() {
    private val _state = mutableStateOf(AddProductState())
    val state: State<AddProductState> = _state

    fun onNameChange(newName: String) {
        _state.value = _state.value.copy(name = newName)
        validate()
    }

    fun onDateChange(newDate: String) {
        _state.value = _state.value.copy(expirationDate = newDate)
        validate()
    }

    fun onLocationChange(newLocation: Location) {
        _state.value = _state.value.copy(location = newLocation)
        validate()
    }

    private fun validate() {
        val isNameValid = _state.value.name.isNotBlank()
        val isDateValid = try {
            LocalDate.parse(_state.value.expirationDate)
            true
        } catch (e: DateTimeParseException) {
            false
        }
        _state.value = _state.value.copy(isValid = isNameValid && isDateValid)
    }

    // Cette fonction transforme l'état en objet Product final
    fun getProduct(): Product? {
        return if (_state.value.isValid) {
            Product(
                id = System.currentTimeMillis(),
                name = _state.value.name,
                expiredDate = LocalDate.parse(_state.value.expirationDate),
                location = _state.value.location
            )
        } else null
    }
}