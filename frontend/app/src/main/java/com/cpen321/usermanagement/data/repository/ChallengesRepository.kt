package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.Challenge

interface ChallengesRepository {

    suspend fun getChallenges(): Result<List<Challenge>>


}