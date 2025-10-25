package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import retrofit2.Response
import retrofit2.http.*

interface TicketsInterface {

    @GET("tickets/user/{userId}")
    suspend fun getTickets(
        @Path("userId") userId: String
    ): Response<List<BingoTicket>>

    @GET("tickets/{id}")
    suspend fun getTicketById(
        @Path("id") id: String
    ): Response<BingoTicket>

    @POST("tickets")
    suspend fun createTicket(
        @Body ticket: BingoTicket
    ): Response<BingoTicket>

    @DELETE("tickets/{id}")
    suspend fun deleteTicket(
        @Path("id") id: String
    ): Response<Unit>
}
