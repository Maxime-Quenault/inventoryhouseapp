package com.example.inventoryhouse.ui.screen.house

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.inventoryhouse.data.remote.dto.HouseDto
import com.example.inventoryhouse.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HouseViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HouseState())
    val state: StateFlow<HouseState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun onEvent(event: HouseEvent) {
        when (event) {
            HouseEvent.Refresh -> refresh()
            is HouseEvent.SelectHouse -> selectHouse(event.house)
            is HouseEvent.HouseNameChanged -> _state.update {
                it.copy(houseNameInput = event.value, errorMessage = null)
            }
            HouseEvent.CreateHouse -> createHouse()
            is HouseEvent.MemberEmailChanged -> _state.update {
                it.copy(memberEmailInput = event.value.trim(), errorMessage = null)
            }
            is HouseEvent.MemberRoleChanged -> _state.update {
                it.copy(memberRoleInput = event.value, errorMessage = null)
            }
            HouseEvent.AddMember -> addMember()
            is HouseEvent.RemoveMember -> removeMember(event.member.userId)
            HouseEvent.ClearFeedback -> _state.update {
                it.copy(errorMessage = null, successMessage = null)
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = repository.me()
                val houses = repository.getHouses()
                val currentId = _state.value.selectedHouse?.id
                val selected = houses.firstOrNull { it.id == currentId } ?: houses.firstOrNull()
                val members = selected?.let { repository.getMembers(it.id) }.orEmpty()

                _state.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        houses = houses,
                        selectedHouse = selected,
                        members = members,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Impossible de charger la maison"
                    )
                }
            }
        }
    }

    private fun selectHouse(house: HouseDto) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, selectedHouse = house, errorMessage = null) }
            try {
                val members = repository.getMembers(house.id)
                _state.update {
                    it.copy(isLoading = false, selectedHouse = house, members = members)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Impossible de charger les membres"
                    )
                }
            }
        }
    }

    private fun createHouse() {
        val name = _state.value.houseNameInput.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                val house = repository.createHouse(name = name)
                val houses = repository.getHouses()
                val members = repository.getMembers(house.id)
                _state.update {
                    it.copy(
                        isLoading = false,
                        houses = houses.ifEmpty { listOf(house) },
                        selectedHouse = house,
                        members = members,
                        houseNameInput = "",
                        successMessage = "Maison creee"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Impossible de creer la maison"
                    )
                }
            }
        }
    }

    private fun addMember() {
        val state = _state.value
        val house = state.selectedHouse ?: return
        val email = state.memberEmailInput.trim()
        if (email.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                repository.addMember(
                    houseId = house.id,
                    email = email,
                    role = state.memberRoleInput
                )
                val members = repository.getMembers(house.id)
                _state.update {
                    it.copy(
                        isLoading = false,
                        members = members,
                        memberEmailInput = "",
                        memberRoleInput = "member",
                        successMessage = "Membre ajoute"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Impossible d'ajouter ce membre"
                    )
                }
            }
        }
    }

    private fun removeMember(userId: Long) {
        val house = _state.value.selectedHouse ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                repository.removeMember(houseId = house.id, userId = userId)
                val members = repository.getMembers(house.id)
                _state.update {
                    it.copy(
                        isLoading = false,
                        members = members,
                        successMessage = "Membre retire"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Impossible de retirer ce membre"
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(repository: InventoryRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HouseViewModel(repository)
            }
        }
    }
}
