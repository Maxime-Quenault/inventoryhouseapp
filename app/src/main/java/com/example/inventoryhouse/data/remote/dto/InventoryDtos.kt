package com.example.inventoryhouse.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MessageResponseDto(
    val message: String? = null
)

data class MeResponseDto(
    val user: UserDto? = null
)

data class HousesResponseDto(
    val houses: List<HouseDto> = emptyList()
)

data class HouseResponseDto(
    val house: HouseDto? = null
)

data class CreateHouseRequestDto(
    val name: String,
    val type: String = "home"
)

data class UpdateHouseRequestDto(
    val name: String? = null,
    val type: String? = null
)

data class HouseDto(
    val id: Long = 0,
    val name: String = "",
    val type: String? = null,
    @SerializedName("created_by")
    val createdBy: Long? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    val role: String? = null,
    @SerializedName("joined_at")
    val joinedAt: String? = null,
    val counts: HouseCountsDto? = null
)

data class HouseCountsDto(
    val members: Int = 0,
    val items: Int = 0
)

data class MembersResponseDto(
    val members: List<MemberDto> = emptyList()
)

data class MemberResponseDto(
    val member: MemberDto? = null
)

data class AddMemberRequestDto(
    val email: String,
    val role: String = "member"
)

data class UpdateMemberRoleRequestDto(
    val role: String
)

data class MemberDto(
    @SerializedName("user_id")
    val userId: Long = 0,
    @SerializedName("house_id")
    val houseId: Long = 0,
    @SerializedName("joined_at")
    val joinedAt: String? = null,
    val role: MemberRoleDto? = null,
    val user: UserDto? = null
)

data class MemberRoleDto(
    val id: Long = 0,
    val name: String = "member"
)

data class ReferenceDto(
    val id: Long = 0,
    val name: String = ""
)

data class ReferencesResponseDto(
    val locations: List<ReferenceDto> = emptyList(),
    @SerializedName("stock_categories")
    val stockCategories: List<ReferenceDto> = emptyList()
)

data class ItemsResponseDto(
    val items: List<ItemDto> = emptyList()
)

data class ItemResponseDto(
    val item: ItemDto? = null
)

data class CreateItemRequestDto(
    @SerializedName("category_id")
    val categoryId: Long,
    @SerializedName("location_id")
    val locationId: Long,
    val name: String,
    val quantity: Int = 0,
    val unit: String,
    @SerializedName("expiration_date")
    val expirationDate: String? = null
)

data class UpdateItemRequestDto(
    @SerializedName("category_id")
    val categoryId: Long? = null,
    @SerializedName("location_id")
    val locationId: Long? = null,
    val name: String? = null,
    val quantity: Int? = null,
    val unit: String? = null,
    @SerializedName("expiration_date")
    val expirationDate: String? = null
)

data class ItemDto(
    val id: Long = 0,
    @SerializedName("house_id")
    val houseId: Long = 0,
    @SerializedName("category_id")
    val categoryId: Long = 0,
    @SerializedName("location_id")
    val locationId: Long = 0,
    val name: String = "",
    val quantity: Int = 0,
    val unit: String = "piece",
    @SerializedName("expiration_date")
    val expirationDate: String? = null,
    @SerializedName("created_by")
    val createdBy: Long? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("deleted_at")
    val deletedAt: String? = null,
    val category: ReferenceDto? = null,
    val location: ReferenceDto? = null,
    @SerializedName("created_by_user")
    val createdByUser: UserDto? = null
)

data class StockMovementsResponseDto(
    @SerializedName("stock_movements")
    val stockMovements: List<StockMovementDto> = emptyList()
)

data class StockMovementDto(
    val id: Long = 0,
    @SerializedName("item_id")
    val itemId: Long = 0,
    @SerializedName("user_id")
    val userId: Long = 0,
    @SerializedName("house_id")
    val houseId: Long = 0,
    @SerializedName("change_quantity")
    val changeQuantity: Int = 0,
    val action: String = "",
    @SerializedName("created_at")
    val createdAt: String? = null,
    val item: StockMovementItemDto? = null,
    val user: UserDto? = null
)

data class StockMovementItemDto(
    val id: Long = 0,
    val name: String = "",
    val unit: String = "piece",
    @SerializedName("deleted_at")
    val deletedAt: String? = null
)
