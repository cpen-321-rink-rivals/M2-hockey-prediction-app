package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.Challenge
import com.cpen321.usermanagement.data.remote.dto.CreateChallengeRequest

interface ChallengesRepository {

    suspend fun getChallenges(): Result<List<Challenge>>

    suspend fun createChallenge(challengeRequest: CreateChallengeRequest): Result<Challenge>
}


