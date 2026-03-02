package com.example.inventoryhouse.data.repository

import com.example.inventoryhouse.data.local.session.SessionStore
import com.example.inventoryhouse.data.remote.api.AuthApi
import com.example.inventoryhouse.data.remote.dto.AuthResponseDto
import com.example.inventoryhouse.data.remote.dto.ErrorResponseDto
import com.example.inventoryhouse.data.remote.dto.LoginRequestDto
import com.example.inventoryhouse.data.remote.dto.RegisterRequestDto
import com.example.inventoryhouse.data.remote.dto.UserDto
import com.example.inventoryhouse.domain.repository.AuthRepository
import com.google.gson.Gson
import retrofit2.Response

class RemoteAuthRepository(
    private val api: AuthApi,
    private val sessionStore: SessionStore,
    private val gson: Gson = Gson()
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResponseDto {
        val resp = api.login(LoginRequestDto(email = email, password = password))

        if (resp.isSuccessful) {
            val body = resp.body() ?: throw Exception("Réponse vide du serveur")
            sessionStore.saveToken(body.token)
            return body
        }

        // Essaie de lire { "error": "..." }
        val errorMsg = try {
            val errJson = resp.errorBody()?.string()
            if (errJson.isNullOrBlank()) "Erreur de connexion"
            else gson.fromJson(errJson, ErrorResponseDto::class.java).error
        } catch (_: Exception) {
            "Erreur de connexion (${resp.code()})"
        }

        throw Exception(errorMsg)
    }

    override suspend fun logout() {
        sessionStore.clearToken()
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String,
        confirmPassword: String
    ): AuthResponseDto {

        val resp = api.register(
            RegisterRequestDto(
                email = email,
                password = password,
                name = name,
                confirmPassword = confirmPassword
            )
        )

        if (resp.isSuccessful) {
            val body = resp.body() ?: throw Exception("Réponse vide du serveur")
            sessionStore.saveToken(body.token)
            return body
        }

        // Essaie de lire { "error": "..." }
        val errorMsg = try {
            val errJson = resp.errorBody()?.string()
            if (errJson.isNullOrBlank()) {
                "Erreur lors de l'inscription"
            } else {
                gson.fromJson(errJson, ErrorResponseDto::class.java).error
                    ?: "Erreur lors de l'inscription"
            }
        } catch (_: Exception) {
            "Erreur lors de l'inscription (${resp.code()})"
        }

        throw Exception(errorMsg)
    }

    private fun requireTokenOrThrow(response: Response<* /* AuthResponseDto */>): String {
        if (response.isSuccessful) {
            val body = response.body()
            // on “cast” proprement sans dépendre de generics compliqués
            val token = (body as? com.example.inventoryhouse.data.remote.dto.AuthResponseDto)?.token
            if (!token.isNullOrBlank()) return token
            throw RuntimeException("Réponse invalide : token manquant.")
        }

        val message = parseErrorMessage(response) ?: "Erreur (${response.code()})"
        throw RuntimeException(message)
    }

    private fun parseErrorMessage(response: Response<*>): String? {
        return try {
            val raw = response.errorBody()?.string().orEmpty()
            if (raw.isBlank()) return null
            gson.fromJson(raw, ErrorResponseDto::class.java).error
        } catch (_: Exception) {
            null
        }
    }
}