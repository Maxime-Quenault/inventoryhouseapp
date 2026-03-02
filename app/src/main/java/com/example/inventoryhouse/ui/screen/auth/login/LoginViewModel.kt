package com.example.inventoryhouse.ui.screen.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventoryhouse.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun onEvent(event: LoginEvent, onSuccess: () -> Unit) {
        when (event) {
            is LoginEvent.EmailChanged ->
                _state.update { it.copy(email = event.value.trim(), errorMessage = null) }

            is LoginEvent.PasswordChanged ->
                _state.update { it.copy(password = event.value, errorMessage = null) }

            LoginEvent.TogglePasswordVisibility ->
                _state.update { it.copy(showPassword = !it.showPassword) }

            LoginEvent.ClearError ->
                _state.update { it.copy(errorMessage = null) }

            LoginEvent.Submit -> submit { _ -> onSuccess() }
        }
    }

    fun submit(onSuccess: (token: String) -> Unit) {
        val current = _state.value
        if (!current.canSubmit) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = authRepository.login(
                    email = current.email,
                    password = current.password
                )
                _state.update { it.copy(isLoading = false) }
                onSuccess(result.token)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message ?: "Erreur de connexion") }
            }
        }
    }
}
