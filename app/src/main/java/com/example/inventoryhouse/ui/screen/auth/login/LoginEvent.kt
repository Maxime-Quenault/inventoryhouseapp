package com.example.inventoryhouse.ui.screen.auth.login

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object TogglePasswordVisibility : LoginEvent
    data object Submit : LoginEvent
    data object ClearError : LoginEvent
}