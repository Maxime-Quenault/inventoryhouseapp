package com.example.inventoryhouse.ui.screen.profile

sealed interface ProfileEvent {
    data object InviteMember : ProfileEvent
    data object EditHousehold : ProfileEvent
}
