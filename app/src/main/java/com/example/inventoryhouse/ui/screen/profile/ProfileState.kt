package com.example.inventoryhouse.ui.screen.profile

import com.example.inventoryhouse.data.enums.Location

data class ProfileState(
    val householdName: String = "Ma maison",
    val memberCount: Int = 1,
    val totalItems: Int = 0,
    val expiringSoonCount: Int = 0,
    val locations: List<LocationStockSummary> = emptyList()
)

data class LocationStockSummary(
    val location: Location,
    val count: Int
)
