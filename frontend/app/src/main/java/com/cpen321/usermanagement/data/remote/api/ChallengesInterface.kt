package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.Challenge
import com.cpen321.usermanagement.data.remote.dto.CreateChallengeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ChallengesInterface {

    @GET("challenges/user")
    suspend fun getChallenges(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Map<String, List<Challenge>>>>
    /* RESPONSE:
    Divided by status like:
    {
        "pending": [],
        "active": [],
        "live": [],
        "finished": [],
        "cancelled": []
    }
    */

    @GET("challenges/{challengeId}")
    suspend fun getChallenge(
        @Header("Authorization") token: String,
        @Path("challengeId") challengeId: String
    ): Response<ApiResponse<Challenge>> // gets a specific challenge

    @POST("challenges")
    suspend fun createChallenge(
        @Header("Authorization") token: String,
        @Body request: CreateChallengeRequest
    ): Response<ApiResponse<Challenge>> // creates a new challenge and returns it.


    @PUT("challenges/{challengeId}")
    suspend fun updateChallenge(
        @Header("Authorization") token: String,
        @Path("challengeId") challengeId: String,
        @Body updatedChallenge: Challenge
    ): Response<ApiResponse<Challenge>> // updates a challenge and returns it.


    @DELETE("challenges/{challengeId}")
    suspend fun deleteChallenge(
        @Header("Authorization") token: String,
        @Path("challengeId") challengeId: String
    ): Response<ApiResponse<Unit>> // deletes a challenge
}

