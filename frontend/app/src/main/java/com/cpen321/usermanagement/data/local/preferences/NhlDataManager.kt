package com.cpen321.usermanagement.data.local.preferences

import android.util.Log
import com.cpen321.usermanagement.data.remote.dto.Game
import com.cpen321.usermanagement.data.remote.dto.GameDay
import com.cpen321.usermanagement.data.repository.NHLRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import com.cpen321.usermanagement.data.remote.dto.PlayerInfo
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UI state for NHL data management
 */
data class NhlDataState(
    val isLoading: Boolean = false,
    val gameSchedule: List<GameDay>? = null,
    val errorMessage: String? = null
)

enum class EventCategory { FORWARD, DEFENSE, GOALIE, TEAM, PENALTY }
enum class ComparisonType { GREATER_THAN, LESS_THAN }

data class EventCondition(
    val id: String, // unique for UI
    val category: EventCategory,
    val subject: String, // e.g. "player.goals"
    val comparison: ComparisonType,
    val threshold: Double,
    val label: String
)

/**
 * Shared manager for NHL schedule data across the app
 * Maintains a single source of truth for game schedule
 */
@Singleton
class NhlDataManager @Inject constructor(
    private val nhlRepository: NHLRepository
) {
    companion object {
        private const val TAG = "NhlDataManager"
    }

    private val _uiState = MutableStateFlow(NhlDataState())
    val uiState: StateFlow<NhlDataState> = _uiState.asStateFlow()

    private val eventPool = listOf(
        // Forwards
        EventCondition("F1", EventCategory.FORWARD, "player.goals", ComparisonType.GREATER_THAN, 1.0, "Forward scores more than 1 goal"),
        EventCondition("F2", EventCategory.FORWARD, "player.assists", ComparisonType.GREATER_THAN, 1.0, "Forward gets more than 1 assist"),
        EventCondition("F3", EventCategory.FORWARD, "player.hits", ComparisonType.GREATER_THAN, 3.0, "Forward makes more than 3 hits"),
        EventCondition("F4", EventCategory.FORWARD, "player.sog", ComparisonType.GREATER_THAN, 4.0, "Forward has more than 4 shots on goal"),
        EventCondition("F5", EventCategory.FORWARD, "player.penaltyMinutes", ComparisonType.GREATER_THAN, 2.0, "Forward gets a penalty"),

        // Defense
        EventCondition("D1", EventCategory.DEFENSE, "player.blockedShots", ComparisonType.GREATER_THAN, 2.0, "Defense blocks more than 2 shots"),
        EventCondition("D2", EventCategory.DEFENSE, "player.toi", ComparisonType.GREATER_THAN, 20.0, "Defense plays over 20 minutes"),
        EventCondition("D3", EventCategory.DEFENSE, "player.hits", ComparisonType.GREATER_THAN, 2.0, "Defense makes more than 2 hits"),

        // Goalies
        EventCondition("G1", EventCategory.GOALIE, "player.saves", ComparisonType.GREATER_THAN, 25.0, "Goalie makes more than 25 saves"),
        EventCondition("G2", EventCategory.GOALIE, "player.savePct", ComparisonType.GREATER_THAN, 0.92, "Goalie save % > 0.92"),
        EventCondition("G3", EventCategory.GOALIE, "player.goalsAgainst", ComparisonType.LESS_THAN, 2.0, "Goalie allows fewer than 2 goals"),

        // Team
        EventCondition("T1", EventCategory.TEAM, "team.goals", ComparisonType.GREATER_THAN, 3.0, "Team scores more than 3 goals"),
        EventCondition("T2", EventCategory.TEAM, "team.sog", ComparisonType.GREATER_THAN, 25.0, "Team has more than 25 shots on goal"),
        EventCondition("T3", EventCategory.TEAM, "team.faceoffWinningPctg", ComparisonType.GREATER_THAN, 55.0, "Team wins over 55% of faceoffs"),

        // Penalties
        EventCondition("P1", EventCategory.PENALTY, "team.penaltyMinutes", ComparisonType.GREATER_THAN, 10.0, "Team takes over 10 penalty minutes"),
        EventCondition("P2", EventCategory.PENALTY, "player.penalties", ComparisonType.GREATER_THAN, 1.0, "Player commits more than 1 penalty")
    )

    /**
     * Load current NHL schedule
     */
    suspend fun loadSchedule(): Result<List<GameDay>> {
        return try {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = nhlRepository.getCurrentSchedule()

            if (result.isSuccess) {
                val schedule = result.getOrNull() ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    gameSchedule = schedule,
                    errorMessage = null
                )
                Log.d(TAG, "Schedule loaded successfully: ${schedule.size} game weeks")
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Failed to load schedule"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
                Log.e(TAG, "Failed to load schedule: $errorMessage")
            }

            result
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message
            )
            Log.e(TAG, "Error loading schedule", e)
            Result.failure(e)
        }
    }

    /**
     * Get all upcoming games (future games only)
     */
    fun getUpcomingGames(): List<Game> {
        return _uiState.value.gameSchedule?.flatMap { gameWeek ->
            gameWeek.games.filter { game ->
                game.gameState == "FUT" // Future games only
            }
        } ?: emptyList()
    }

    /**
     * Get games for challenge creation (upcoming games only)
     */
    fun getGamesForChallenges(): List<Game> {
        return getUpcomingGames().take(20) // Limit to next 20 games
    }

    /**
     * Get games for ticket creation
     */
    fun getGamesForTickets(): List<Game> {
        return getUpcomingGames().take(20)
    }

    suspend fun getEventsForGame(gameId: Long, count: Int = 30): List<EventCondition> = coroutineScope {
        val game = getGameById(gameId) ?: return@coroutineScope emptyList()
        val random = Random(gameId)

        // Fetch both rosters concurrently
        val homeRosterDeferred = async { nhlRepository.getTeamRoster(game.homeTeam.abbrev).getOrNull() }
        val awayRosterDeferred = async { nhlRepository.getTeamRoster(game.awayTeam.abbrev).getOrNull() }

        val homeRoster = homeRosterDeferred.await()
        val awayRoster = awayRosterDeferred.await()

        // Collect player groups
        val forwards = mutableListOf<PlayerInfo>()
        val defensemen = mutableListOf<PlayerInfo>()
        val goalies = mutableListOf<PlayerInfo>()
        val teams = listOf(game.homeTeam.commonName, game.awayTeam.commonName)

        homeRoster?.let {
            forwards += it.forwards
            defensemen += it.defensemen
            goalies += it.goalies
        }
        awayRoster?.let {
            forwards += it.forwards
            defensemen += it.defensemen
            goalies += it.goalies
        }

        // Group event templates by category
        val categories = eventPool.groupBy { it.category }

        val balancedTemplates = categories.flatMap { (_, list) ->
            list.shuffled(random).take((count / categories.size).coerceAtLeast(1))
        }

        // Replace placeholders with real player or team names
        val personalized = balancedTemplates.map { template ->
            val label = when (template.category) {
                EventCategory.FORWARD -> template.label.replace(
                    "Forward",
                    forwards.randomOrNull(random)?.fullName ?: "Forward"
                )
                EventCategory.DEFENSE -> template.label.replace(
                    "Defense",
                    defensemen.randomOrNull(random)?.fullName ?: "Defense"
                )
                EventCategory.GOALIE -> template.label.replace(
                    "Goalie",
                    goalies.randomOrNull(random)?.fullName ?: "Goalie"
                )
                EventCategory.TEAM, EventCategory.PENALTY -> template.label.replace(
                    "Team",
                    template.label.replace("Team", teams.randomOrNull(random)?.default ?: "Team")
                )
            }
            template.copy(label = label)
        }

        personalized.shuffled(random).take(count)
    }

    /**
     * Get a specific game by ID
     */
    fun getGameById(gameId: Long): Game? {
        return _uiState.value.gameSchedule?.flatMap { it.games }?.find { it.id == gameId }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Refresh the schedule data
     */
    suspend fun refreshSchedule(): Result<List<GameDay>> {
        return loadSchedule()
    }
}