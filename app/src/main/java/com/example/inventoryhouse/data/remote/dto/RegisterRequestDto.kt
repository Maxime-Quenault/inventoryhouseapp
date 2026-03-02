package com.example.inventoryhouse.data.remote.dto

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val name: String,
    val confirmPassword: String
)