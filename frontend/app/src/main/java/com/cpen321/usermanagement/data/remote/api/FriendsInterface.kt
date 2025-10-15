package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.FriendData
import com.cpen321.usermanagement.data.remote.dto.PendingRequestData
import retrofit2.Response
import retrofit2.http.*

interface FriendsInterface {
    @POST("friends/request")
    suspend fun sendFriendRequest(
        @Header("Authorization") authHeader: String,
        @Body body: Map<String, String> // e.g. {"receiverCode": "1234-5678"}
    ): Response<ApiResponse<Unit>>

    @POST("friends/accept")
    suspend fun acceptFriendRequest(
        @Header("Authorization") authHeader: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>

    @POST("friends/reject")
    suspend fun rejectFriendRequest(
        @Header("Authorization") authHeader: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>

    @DELETE("friends/{friendId}")
    suspend fun removeFriend(
        @Header("Authorization") authHeader: String,
        @Path("friendId") friendId: String
    ): Response<ApiResponse<Unit>>

    @GET("friends/list")
    suspend fun getFriends(
        @Header("Authorization") authHeader: String
    ): Response<ApiResponse<List<FriendData>>>

    @GET("friends/pending")
    suspend fun getPendingRequests(
        @Header("Authorization") authHeader: String
    ): Response<ApiResponse<List<PendingRequestData>>>
}
