package com.example.inventoryhouse.data.remote.network

import com.example.inventoryhouse.data.remote.api.AuthApi
import com.example.inventoryhouse.data.remote.api.OpenFoodFactsApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val appRetrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val foodFactsRetrofit = Retrofit.Builder()
        .baseUrl("https://world.openfoodfacts.net/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = appRetrofit.create(AuthApi::class.java)
    val openFoodFactsApi: OpenFoodFactsApi = foodFactsRetrofit.create(OpenFoodFactsApi::class.java)
}
