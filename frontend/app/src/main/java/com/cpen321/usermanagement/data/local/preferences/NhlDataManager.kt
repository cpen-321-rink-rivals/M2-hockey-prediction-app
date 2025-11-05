package com.cpen321.usermanagement.data.local.preferences

import com.cpen321.usermanagement.data.remote.dto.Game
import com.cpen321.usermanagement.data.remote.dto.GameDay
import kotlinx.coroutines.flow.StateFlow
import com.cpen321.usermanagement.data.local.preferences.NhlDataState

interface NhlDataManager {
    val uiState: StateFlow<NhlDataState>
    suspend fun loadSchedule(): Result<List<GameDay>>
    fun getUpcomingGames(): List<Game>
    fun getGamesForChallenges(): List<Game>
    fun getGamesForTickets(): List<Game>
    suspend fun getEventsForGame(gameId: Long, count: Int = 30): List<EventCondition>
    fun getGameById(gameId: Long): Game?
    fun clearError()
    suspend fun refreshSchedule(): Result<List<GameDay>>
}
