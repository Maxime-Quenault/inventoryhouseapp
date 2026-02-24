package com.example.inventoryhouse.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val icon: ImageVector
) {
    HOME(
        label = "Maison",
        icon = Icons.Default.Home
    ),
    STOCK(
        label = "Stock",
        icon = Icons.AutoMirrored.Filled.FormatListBulleted
    ),
    ADD_PRODUCT(
        label = "Scan",
        icon = Icons.Default.QrCodeScanner
    ),
    FOOD(
        label = "Recettes",
        icon = Icons.Default.FoodBank
    ),
    PROFILE(
        label = "Profile",
        icon = Icons.Default.AccountBox
    ),
}