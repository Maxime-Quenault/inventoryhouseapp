package com.example.inventoryhouse.data.remote.api

import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.Body
import com.example.inventoryhouse.data.remote.dto.RegisterRequestDto
import com.example.inventoryhouse.data.remote.dto.AuthResponseDto
import com.example.inventoryhouse.data.remote.dto.LoginRequestDto

interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(
        @Body registerRequestDto: RegisterRequestDto
    ): Response<AuthResponseDto>

    @POST("api/auth/login")
    suspend fun login(
        @Body loginRequestDto: LoginRequestDto
    ): Response<AuthResponseDto>
}