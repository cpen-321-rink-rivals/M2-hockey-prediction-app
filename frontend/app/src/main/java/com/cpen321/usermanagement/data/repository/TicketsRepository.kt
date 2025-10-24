package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.api.TicketsInterface
import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import com.cpen321.usermanagement.data.remote.dto.Game
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketsRepository @Inject constructor(
    private val api: TicketsInterface
) {

    suspend fun getTickets(userId: String): Result<List<BingoTicket>> = try {
        val response = api.getTickets(userId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Failed to load tickets"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createTicket(
        userId: String,
        name: String,
        game: Game,
        events: List<String>
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

    suspend fun deleteTicket(ticketId: String): Result<Unit> = try {
        val response = api.deleteTicket(ticketId)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to delete ticket"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
