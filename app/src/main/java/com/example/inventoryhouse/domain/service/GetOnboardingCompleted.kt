package com.example.inventoryhouse.domain.service

import com.example.inventoryhouse.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class GetOnboardingCompleted(
    private val repository: OnboardingRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.onboardingCompleted
}