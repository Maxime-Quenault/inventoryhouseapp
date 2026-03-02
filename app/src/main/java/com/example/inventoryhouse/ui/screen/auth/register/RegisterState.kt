package com.example.inventoryhouse.ui.screen.auth.register

data class RegisterState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val showPassword: Boolean = false,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val passwordOk: Boolean get() = password.length >= 6
    val matchOk: Boolean get() = confirmPassword.isNotBlank() && password == confirmPassword

    val canSubmit: Boolean
        get() = name.isNotBlank() &&
                email.isNotBlank() &&
                passwordOk &&
                matchOk &&
                !isLoading
}