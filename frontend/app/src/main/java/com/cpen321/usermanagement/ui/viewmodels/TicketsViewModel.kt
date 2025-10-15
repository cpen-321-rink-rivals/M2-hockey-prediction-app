package com.cpen321.usermanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// You can expand this later if your Tickets screen needs to manage state
data class TicketsUiState(
    val isLoadingTickets: Boolean = false,

    val allTickets: List<String> = emptyList(),

    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Add any other state relevant to the tickets screen, e.g., list of tickets
)

private val allTickets = listOf(
    "22/9 Oilers vs. Canucks",
    "23/9 Canucks vs. Sabres",
    "24/10 Wednesday against Thomas and friends",
    "20/10 Montréal vs. Sabres",
    "22/10 Lets go Canucks!!"
)


@HiltViewModel
class TicketsViewModel @Inject constructor(
    // Inject repositories here if needed in the future
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketsUiState(
        isLoadingTickets = false, // TODO: Actually load the tickets with new function
        allTickets = allTickets
    ))
    val uiState = _uiState.asStateFlow()

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}