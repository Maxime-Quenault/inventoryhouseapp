package com.example.inventoryhouse.data.remote.api

import com.example.inventoryhouse.data.remote.dto.AddMemberRequestDto
import com.example.inventoryhouse.data.remote.dto.CreateHouseRequestDto
import com.example.inventoryhouse.data.remote.dto.CreateItemRequestDto
import com.example.inventoryhouse.data.remote.dto.HouseResponseDto
import com.example.inventoryhouse.data.remote.dto.HousesResponseDto
import com.example.inventoryhouse.data.remote.dto.ItemResponseDto
import com.example.inventoryhouse.data.remote.dto.ItemsResponseDto
import com.example.inventoryhouse.data.remote.dto.MeResponseDto
import com.example.inventoryhouse.data.remote.dto.MemberResponseDto
import com.example.inventoryhouse.data.remote.dto.MembersResponseDto
import com.example.inventoryhouse.data.remote.dto.MessageResponseDto
import com.example.inventoryhouse.data.remote.dto.ReferencesResponseDto
import com.example.inventoryhouse.data.remote.dto.StockMovementsResponseDto
import com.example.inventoryhouse.data.remote.dto.UpdateHouseRequestDto
import com.example.inventoryhouse.data.remote.dto.UpdateItemRequestDto
import com.example.inventoryhouse.data.remote.dto.UpdateMemberRoleRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InventoryApi {

    @GET("api/auth/me")
    suspend fun me(): Response<MeResponseDto>

    @POST("api/auth/logout")
    suspend fun logout(): Response<MessageResponseDto>

    @GET("api/houses")
    suspend fun getHouses(): Response<HousesResponseDto>

    @POST("api/houses")
    suspend fun createHouse(@Body body: CreateHouseRequestDto): Response<HouseResponseDto>

    @GET("api/houses/{houseId}")
    suspend fun getHouse(@Path("houseId") houseId: Long): Response<HouseResponseDto>

    @PATCH("api/houses/{houseId}")
    suspend fun updateHouse(
        @Path("houseId") houseId: Long,
        @Body body: UpdateHouseRequestDto
    ): Response<HouseResponseDto>

    @DELETE("api/houses/{houseId}")
    suspend fun deleteHouse(@Path("houseId") houseId: Long): Response<MessageResponseDto>

    @GET("api/houses/{houseId}/members")
    suspend fun getMembers(@Path("houseId") houseId: Long): Response<MembersResponseDto>

    @POST("api/houses/{houseId}/members")
    suspend fun addMember(
        @Path("houseId") houseId: Long,
        @Body body: AddMemberRequestDto
    ): Response<MemberResponseDto>

    @PATCH("api/houses/{houseId}/members/{userId}")
    suspend fun updateMemberRole(
        @Path("houseId") houseId: Long,
        @Path("userId") userId: Long,
        @Body body: UpdateMemberRoleRequestDto
    ): Response<MemberResponseDto>

    @DELETE("api/houses/{houseId}/members/{userId}")
    suspend fun removeMember(
        @Path("houseId") houseId: Long,
        @Path("userId") userId: Long
    ): Response<MessageResponseDto>

    @GET("api/houses/{houseId}/items")
    suspend fun getItems(
        @Path("houseId") houseId: Long,
        @Query("location_id") locationId: Long? = null,
        @Query("category_id") categoryId: Long? = null,
        @Query("search") search: String? = null,
        @Query("updated_since") updatedSince: String? = null
    ): Response<ItemsResponseDto>

    @POST("api/houses/{houseId}/items")
    suspend fun createItem(
        @Path("houseId") houseId: Long,
        @Body body: CreateItemRequestDto
    ): Response<ItemResponseDto>

    @PATCH("api/houses/{houseId}/items/{itemId}")
    suspend fun updateItem(
        @Path("houseId") houseId: Long,
        @Path("itemId") itemId: Long,
        @Body body: UpdateItemRequestDto
    ): Response<ItemResponseDto>

    @DELETE("api/houses/{houseId}/items/{itemId}")
    suspend fun deleteItem(
        @Path("houseId") houseId: Long,
        @Path("itemId") itemId: Long
    ): Response<MessageResponseDto>

    @GET("api/houses/{houseId}/stock-movements")
    suspend fun getStockMovements(
        @Path("houseId") houseId: Long,
        @Query("since") since: String? = null,
        @Query("action") action: String? = null,
        @Query("limit") limit: Int? = 50
    ): Response<StockMovementsResponseDto>

    @GET("api/references")
    suspend fun getReferences(): Response<ReferencesResponseDto>
}
