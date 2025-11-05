package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import com.cpen321.usermanagement.data.remote.dto.Game

interface TicketsRepository {
    suspend fun getTickets(userId: String): Result<List<BingoTicket>>
    suspend fun getTicketById(ticketId: String): Result<BingoTicket>
    suspend fun createTicket(
        userId: String,
        name: String,
        game: Game,
        events: List<String>
    ): Result<BingoTicket>
    suspend fun deleteTicket(ticketId: String): Result<Unit>
    suspend fun updateCrossedOff(
        ticketId: String,
        crossedOff: List<Boolean>
    ): Result<BingoTicket>
}
