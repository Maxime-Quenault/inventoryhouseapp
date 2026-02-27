package com.example.inventoryhouse.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "app_prefs")

object PrefKeys {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
}