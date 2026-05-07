package com.example.inventoryhouse.ui.screen.house

import com.example.inventoryhouse.data.remote.dto.HouseDto
import com.example.inventoryhouse.data.remote.dto.MemberDto
import com.example.inventoryhouse.data.remote.dto.UserDto

data class HouseState(
    val isLoading: Boolean = true,
    val user: UserDto? = null,
    val houses: List<HouseDto> = emptyList(),
    val selectedHouse: HouseDto? = null,
    val members: List<MemberDto> = emptyList(),
    val houseNameInput: String = "",
    val memberEmailInput: String = "",
    val memberRoleInput: String = "member",
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val needsHouse: Boolean
        get() = !isLoading && selectedHouse == null

    val canCreateHouse: Boolean
        get() = houseNameInput.isNotBlank() && !isLoading

    val canAddMember: Boolean
        get() = selectedHouse != null && memberEmailInput.isNotBlank() && !isLoading
}

sealed interface HouseEvent {
    data object Refresh : HouseEvent
    data class SelectHouse(val house: HouseDto) : HouseEvent
    data class HouseNameChanged(val value: String) : HouseEvent
    data object CreateHouse : HouseEvent
    data class MemberEmailChanged(val value: String) : HouseEvent
    data class MemberRoleChanged(val value: String) : HouseEvent
    data object AddMember : HouseEvent
    data class RemoveMember(val member: MemberDto) : HouseEvent
    data object ClearFeedback : HouseEvent
}
