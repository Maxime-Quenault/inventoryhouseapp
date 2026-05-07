package com.example.inventoryhouse.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Long = 0,
    val name: String? = null,
    val email: String = "",
    @SerializedName("auth_provider")
    val authProvider: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)
