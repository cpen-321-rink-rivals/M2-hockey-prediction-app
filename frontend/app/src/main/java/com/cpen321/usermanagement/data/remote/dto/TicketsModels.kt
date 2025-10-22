package com.cpen321.usermanagement.data.remote.dto

// Represents one bingo ticket
data class BingoTicket(
    val _id: String,
    val userId: String,
    val name: String,
    val game: String,
    val events: List<String>
)

// UI state for the TicketsScreen and CreateBingoTicketScreen
data class TicketsUiState(
    val allTickets: List<BingoTicket> = emptyList(),
    val isLoadingTickets: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
