package com.example.inventoryhouse.ui.screen.auth.register

sealed interface RegisterEvent {
    data class NameChanged(val value: String) : RegisterEvent
    data class EmailChanged(val value: String) : RegisterEvent
    data class PasswordChanged(val value: String) : RegisterEvent
    data class ConfirmPasswordChanged(val value: String) : RegisterEvent

    data object TogglePasswordVisibility : RegisterEvent
    data object Submit : RegisterEvent
    data object ClearError : RegisterEvent
}