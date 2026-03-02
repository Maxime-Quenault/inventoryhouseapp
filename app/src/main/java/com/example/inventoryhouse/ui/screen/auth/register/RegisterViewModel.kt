package com.example.inventoryhouse.ui.screen.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventoryhouse.data.remote.dto.AuthResponseDto
import com.example.inventoryhouse.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onEvent(event: RegisterEvent, onSuccess: () -> Unit = {}) {
        when (event) {
            is RegisterEvent.NameChanged ->
                _state.update { it.copy(name = event.value, errorMessage = null) }

            is RegisterEvent.EmailChanged ->
                _state.update { it.copy(email = event.value.trim(), errorMessage = null) }

            is RegisterEvent.PasswordChanged ->
                _state.update { it.copy(password = event.value, errorMessage = null) }

            is RegisterEvent.ConfirmPasswordChanged ->
                _state.update { it.copy(confirmPassword = event.value, errorMessage = null) }

            RegisterEvent.TogglePasswordVisibility ->
                _state.update { it.copy(showPassword = !it.showPassword) }

            RegisterEvent.ClearError ->
                _state.update { it.copy(errorMessage = null) }

            RegisterEvent.Submit -> submit(onSuccess)
        }
    }

    private fun submit(onSuccess: () -> Unit) {
        val s = _state.value

        val localError = when {
            s.name.isBlank() -> "Veuillez renseigner votre nom."
            s.email.isBlank() -> "Veuillez renseigner votre email."
            !s.passwordOk -> "Mot de passe : 6 caractères minimum."
            !s.matchOk -> "Les mots de passe ne correspondent pas."
            else -> null
        }

        if (localError != null) {
            _state.update { it.copy(errorMessage = localError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                authRepository.register(
                    email = s.email.trim(),
                    password = s.password,
                    name = s.name,
                    confirmPassword = s.confirmPassword
                )

                _state.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erreur lors de l'inscription"
                    )
                }
            }
        }
    }
}