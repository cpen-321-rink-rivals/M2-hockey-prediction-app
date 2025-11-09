package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.GameDay
import com.cpen321.usermanagement.data.remote.dto.TeamRosterResponse

interface NHLRepository {

    suspend fun getCurrentSchedule(): Result<List<GameDay>>

    suspend fun getTeamRoster(teamCode: String): Result<TeamRosterResponse>
}