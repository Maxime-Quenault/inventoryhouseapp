package com.example.inventoryhouse.data.repository

import com.example.inventoryhouse.data.remote.api.InventoryApi
import com.example.inventoryhouse.data.remote.dto.AddMemberRequestDto
import com.example.inventoryhouse.data.remote.dto.CreateHouseRequestDto
import com.example.inventoryhouse.data.remote.dto.CreateItemRequestDto
import com.example.inventoryhouse.data.remote.dto.ErrorResponseDto
import com.example.inventoryhouse.data.remote.dto.HouseDto
import com.example.inventoryhouse.data.remote.dto.ItemDto
import com.example.inventoryhouse.data.remote.dto.MemberDto
import com.example.inventoryhouse.data.remote.dto.ReferenceDto
import com.example.inventoryhouse.data.remote.dto.ReferencesResponseDto
import com.example.inventoryhouse.data.remote.dto.StockMovementDto
import com.example.inventoryhouse.data.remote.dto.UpdateItemRequestDto
import com.example.inventoryhouse.data.remote.dto.UpdateMemberRoleRequestDto
import com.example.inventoryhouse.data.remote.dto.UserDto
import com.google.gson.Gson
import retrofit2.Response

class InventoryRepository(
    private val api: InventoryApi,
    private val gson: Gson = Gson()
) {

    suspend fun me(): UserDto {
        return requireBody(api.me()).user ?: throw RuntimeException("Profil introuvable.")
    }

    suspend fun logout() {
        requireBody(api.logout())
    }

    suspend fun getHouses(): List<HouseDto> {
        return requireBody(api.getHouses()).houses
    }

    suspend fun createHouse(name: String, type: String = "home"): HouseDto {
        return requireBody(api.createHouse(CreateHouseRequestDto(name = name, type = type))).house
            ?: throw RuntimeException("Maison introuvable dans la reponse.")
    }

    suspend fun updateHouse(houseId: Long, name: String): HouseDto {
        return requireBody(
            api.updateHouse(
                houseId = houseId,
                body = com.example.inventoryhouse.data.remote.dto.UpdateHouseRequestDto(name = name)
            )
        ).house ?: throw RuntimeException("Maison introuvable dans la reponse.")
    }

    suspend fun getMembers(houseId: Long): List<MemberDto> {
        return requireBody(api.getMembers(houseId)).members
    }

    suspend fun addMember(houseId: Long, email: String, role: String): MemberDto {
        return requireBody(
            api.addMember(
                houseId = houseId,
                body = AddMemberRequestDto(email = email, role = role)
            )
        ).member ?: throw RuntimeException("Membre introuvable dans la reponse.")
    }

    suspend fun updateMemberRole(houseId: Long, userId: Long, role: String): MemberDto {
        return requireBody(
            api.updateMemberRole(
                houseId = houseId,
                userId = userId,
                body = UpdateMemberRoleRequestDto(role = role)
            )
        ).member ?: throw RuntimeException("Membre introuvable dans la reponse.")
    }

    suspend fun removeMember(houseId: Long, userId: Long) {
        requireBody(api.removeMember(houseId = houseId, userId = userId))
    }

    suspend fun getReferences(): ReferencesResponseDto {
        return requireBody(api.getReferences())
    }

    suspend fun getItems(houseId: Long, search: String? = null): List<ItemDto> {
        return requireBody(api.getItems(houseId = houseId, search = search)).items
    }

    suspend fun createItem(
        houseId: Long,
        category: ReferenceDto,
        location: ReferenceDto,
        name: String,
        quantity: Int,
        unit: String,
        expirationDate: String?
    ): ItemDto {
        return requireBody(
            api.createItem(
                houseId = houseId,
                body = CreateItemRequestDto(
                    categoryId = category.id,
                    locationId = location.id,
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    expirationDate = expirationDate
                )
            )
        ).item ?: throw RuntimeException("Item introuvable dans la reponse.")
    }

    suspend fun updateItem(
        houseId: Long,
        itemId: Long,
        category: ReferenceDto? = null,
        location: ReferenceDto? = null,
        name: String? = null,
        quantity: Int? = null,
        unit: String? = null,
        expirationDate: String? = null
    ): ItemDto {
        return requireBody(
            api.updateItem(
                houseId = houseId,
                itemId = itemId,
                body = UpdateItemRequestDto(
                    categoryId = category?.id,
                    locationId = location?.id,
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    expirationDate = expirationDate
                )
            )
        ).item ?: throw RuntimeException("Item introuvable dans la reponse.")
    }

    suspend fun deleteItem(houseId: Long, itemId: Long) {
        requireBody(api.deleteItem(houseId = houseId, itemId = itemId))
    }

    suspend fun getStockMovements(houseId: Long, limit: Int = 20): List<StockMovementDto> {
        return requireBody(api.getStockMovements(houseId = houseId, limit = limit)).stockMovements
    }

    private fun <T> requireBody(response: Response<T>): T {
        if (response.isSuccessful) {
            return response.body() ?: throw RuntimeException("Reponse vide du serveur.")
        }

        val message = try {
            val raw = response.errorBody()?.string().orEmpty()
            if (raw.isBlank()) {
                null
            } else {
                gson.fromJson(raw, ErrorResponseDto::class.java).error
            }
        } catch (_: Exception) {
            null
        }

        throw RuntimeException(message ?: "Erreur API (${response.code()}).")
    }
}
