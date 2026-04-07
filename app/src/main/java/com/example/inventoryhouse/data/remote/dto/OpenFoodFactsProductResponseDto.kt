package com.example.inventoryhouse.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenFoodFactsProductResponseDto(
    val code: String?,
    val status: Int,
    @SerializedName("status_verbose")
    val statusVerbose: String?,
    val product: OpenFoodFactsProductDto?
)

data class OpenFoodFactsProductDto(
    @SerializedName("product_name")
    val productName: String?,
    val brands: String?,
    @SerializedName("image_front_small_url")
    val imageUrl: String?
)
