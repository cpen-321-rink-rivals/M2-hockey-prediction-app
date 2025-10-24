package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.GameDay

interface NHLRepository {

    suspend fun getCurrentSchedule(): Result<List<GameDay>>
}