package com.example.inventoryhouse.data.remote.network

import com.example.inventoryhouse.data.local.session.SessionStore
import com.example.inventoryhouse.data.remote.api.AuthApi
import com.example.inventoryhouse.data.remote.api.InventoryApi
import com.example.inventoryhouse.data.remote.api.OpenFoodFactsApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun client(sessionStore: SessionStore? = null): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = sessionStore?.let { store ->
                    runBlocking { store.tokenFlow.first() }
                }
                val request = if (token.isNullOrBlank()) {
                    chain.request()
                } else {
                    chain.request()
                        .newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                }
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()
    }

    private fun appRetrofit(sessionStore: SessionStore? = null): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://inventoryhouseback.vercel.app/")
            .client(client(sessionStore))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val foodFactsRetrofit = Retrofit.Builder()
        .baseUrl("https://world.openfoodfacts.net/")
        .client(client())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = appRetrofit().create(AuthApi::class.java)
    val openFoodFactsApi: OpenFoodFactsApi = foodFactsRetrofit.create(OpenFoodFactsApi::class.java)

    fun inventoryApi(sessionStore: SessionStore): InventoryApi {
        return appRetrofit(sessionStore).create(InventoryApi::class.java)
    }
}
