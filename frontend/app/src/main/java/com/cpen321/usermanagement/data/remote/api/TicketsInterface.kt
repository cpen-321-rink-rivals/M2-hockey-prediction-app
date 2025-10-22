package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import retrofit2.Response
import retrofit2.http.*

interface TicketsInterface {

    @GET("tickets/{userId}")
    suspend fun getTickets(
        @Path("userId") userId: String
    ): Response<List<BingoTicket>>

    @POST("tickets")
    suspend fun createTicket(
        @Body ticket: BingoTicket
    ): Response<BingoTicket>

    @DELETE("tickets/{id}")
    suspend fun deleteTicket(
        @Path("id") id: String
    ): Response<Unit>
}
