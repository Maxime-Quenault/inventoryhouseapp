package com.example.inventoryhouse.domain.repository

import com.example.inventoryhouse.data.remote.dto.AuthResponseDto

interface AuthRepository {

    /**
     * Appelle POST /api/auth/register
     * et sauvegarde le token en DataStore si succès.
     */
    suspend fun register(
        email: String,
        password: String,
        name: String,
        confirmPassword: String
    ) : AuthResponseDto


    /**
     * Appelle POST /api/auth/login
     * et sauvegarde le token en DataStore si succès.
     */
    suspend fun login(
        email: String,
        password: String
    ) : AuthResponseDto

    suspend fun logout()
}