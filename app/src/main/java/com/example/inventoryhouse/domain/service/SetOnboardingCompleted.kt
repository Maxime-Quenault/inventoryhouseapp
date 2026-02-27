package com.example.inventoryhouse.domain.service

import com.example.inventoryhouse.domain.repository.OnboardingRepository

class SetOnboardingCompleted(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke(completed: Boolean = true) {
        repository.setOnboardingCompleted(completed)
    }
}