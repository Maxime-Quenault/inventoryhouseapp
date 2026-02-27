package com.example.inventoryhouse.ui.screen.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.inventoryhouse.data.repository.OnboardingRepositoryImpl
import com.example.inventoryhouse.domain.service.SetOnboardingCompleted

class OnboardingViewModel(
    private val setOnboardingCompleted: SetOnboardingCompleted
) : ViewModel() {

    suspend fun completeOnboarding() {
        setOnboardingCompleted(true)
    }

    companion object {
        fun factory(appContext: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = OnboardingRepositoryImpl(appContext)
                    val setCompleted = SetOnboardingCompleted(repo)
                    return OnboardingViewModel(setCompleted) as T
                }
            }
        }
    }
}