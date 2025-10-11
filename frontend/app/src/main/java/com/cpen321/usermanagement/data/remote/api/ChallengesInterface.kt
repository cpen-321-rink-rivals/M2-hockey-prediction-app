package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.Challenge
import com.cpen321.usermanagement.data.remote.dto.CreateChallengeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ChallengesInterface {

    // Defines the GET request to the "/challenges" endpoint
    @GET("challenges")
    suspend fun getChallenges(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<Challenge>>>



    // Defines the POST request to the "/challenges" endpoint
    @POST("challenges")
    suspend fun createChallenge(
        @Header("Authorization") token: String,
        @Body request: CreateChallengeRequest
    ): Response<ApiResponse<Challenge>> // Assuming the backend returns the newly created challenge
}

