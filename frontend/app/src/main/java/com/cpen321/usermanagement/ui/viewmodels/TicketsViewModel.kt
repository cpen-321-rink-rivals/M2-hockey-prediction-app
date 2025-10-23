package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import com.cpen321.usermanagement.data.remote.dto.TicketsUiState
import com.cpen321.usermanagement.data.repository.TicketsRepository
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketsViewModel @Inject constructor(
    private val repository: TicketsRepository,
    private val navigationStateManager: NavigationStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketsUiState())
    val uiState: StateFlow<TicketsUiState> = _uiState

    fun loadTickets(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingTickets = true)
            val result = repository.getTickets(userId)
            _uiState.value = _uiState.value.copy(
                isLoadingTickets = false,
                allTickets = result.getOrDefault(emptyList())
            )
        }
    }

    fun createTicket(userId: String, name: String, game: String, events: List<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true)

            val result = repository.createTicket(userId, name, game, events)
            result.onSuccess { newTicket ->
                _uiState.value = _uiState.value.copy(
                    allTickets = _uiState.value.allTickets + newTicket,
                    isCreating = false,
                    successMessage = "Ticket created successfully!"
                )
                navigationStateManager.navigateToTickets()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    error = e.message
                )
            }
        }
    }

    fun deleteTicket(ticketId: String) {
        viewModelScope.launch {
            val result = repository.deleteTicket(ticketId)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    allTickets = _uiState.value.allTickets.filterNot { it._id == ticketId }
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
