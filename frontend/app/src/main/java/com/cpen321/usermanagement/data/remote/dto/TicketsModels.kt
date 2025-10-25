package com.cpen321.usermanagement.data.remote.dto

// Represents one bingo ticket
data class BingoTicket(
    val _id: String,
    val userId: String,
    val name: String,
    val game: Game,
    val events: List<String>,
    val crossedOff: List<Boolean> = List(9) { false }
)

// UI state for the TicketsScreen and CreateBingoTicketScreen
data class TicketsUiState(
    val allTickets: List<BingoTicket> = emptyList(),
    val isLoadingTickets: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,

    val isLoadingGames: Boolean = false,
    val availableGames: List<Game> = emptyList()
)
