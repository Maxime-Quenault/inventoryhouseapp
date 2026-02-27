package com.example.inventoryhouse.data.remote.dto

data class AuthResponseDto(
    val user: UserDto,
    val token: String
)