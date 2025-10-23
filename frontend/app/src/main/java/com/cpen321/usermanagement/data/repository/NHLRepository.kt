package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.Game
import com.cpen321.usermanagement.data.remote.dto.GameWeek

interface NHLRepository {

    suspend fun getCurrentSchedule(): Result<List<GameWeek>>
}