package com.example.inventoryhouse.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.inventoryhouse.data.local.datastore.PrefKeys
import com.example.inventoryhouse.data.local.datastore.dataStore
import com.example.inventoryhouse.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OnboardingRepositoryImpl(
    private val context: Context
) : OnboardingRepository {

    override val onboardingCompleted: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[PrefKeys.ONBOARDING_COMPLETED] ?: false
        }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.ONBOARDING_COMPLETED] = completed
        }
    }
}