package com.example.inventoryhouse.ui.screen.home

sealed interface HomeEvent {
    data object RefreshData : HomeEvent
}
