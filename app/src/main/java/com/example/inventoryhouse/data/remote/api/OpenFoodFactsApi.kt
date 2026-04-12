package com.example.inventoryhouse.data.remote.api

import com.example.inventoryhouse.data.remote.dto.OpenFoodFactsProductResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApi {

    @GET("api/v2/product/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = "product_name,product_quantity,product_quantity_unit,image_front_small_url"
    ): OpenFoodFactsProductResponseDto
}
