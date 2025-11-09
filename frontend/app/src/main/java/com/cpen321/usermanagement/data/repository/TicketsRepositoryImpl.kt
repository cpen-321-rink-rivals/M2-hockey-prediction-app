package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.local.preferences.EventCondition
import com.cpen321.usermanagement.data.remote.api.TicketsInterface
import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import com.cpen321.usermanagement.data.remote.dto.Game
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketsRepositoryImpl @Inject constructor(
    private val api: TicketsInterface
) : TicketsRepository {

    override suspend fun getTickets(userId: String): Result<List<BingoTicket>> = try {
        val response = api.getTickets(userId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to load tickets"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getTicketById(ticketId: String): Result<BingoTicket> = try {
        val response = api.getTicketById(ticketId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to load ticket"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createTicket(
        userId: String,
        name: String,
        game: Game,
        events: List<EventCondition>
    ): Result<BingoTicket> = try {
        val response = api.createTicket(
            BingoTicket(_id = "", userId = userId, name = name, game = game, events = events)
        )
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to create ticket"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteTicket(ticketId: String): Result<Unit> = try {
        val response = api.deleteTicket(ticketId)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to delete ticket"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateCrossedOff(ticketId: String, crossedOff: List<Boolean>): Result<BingoTicket> = try {
        val response = api.updateCrossedOff(ticketId, crossedOff)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to update crossed-off squares"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
