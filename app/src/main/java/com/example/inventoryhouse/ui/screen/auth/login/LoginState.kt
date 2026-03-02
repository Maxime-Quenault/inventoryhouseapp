package com.example.inventoryhouse.ui.screen.auth.login

data class LoginState(
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.length >= 8 && !isLoading
}