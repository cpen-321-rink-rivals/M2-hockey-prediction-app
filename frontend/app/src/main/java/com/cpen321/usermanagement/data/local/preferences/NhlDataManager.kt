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
    val id: String,
    val category: EventCategory,
    val subject: String,
    val comparison: ComparisonType,
    val threshold: Int,
    val labelTemplates: List<String>
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
        // -------- FORWARDS --------
        EventCondition("F1", EventCategory.FORWARD, "player.goals", ComparisonType.GREATER_THAN, 2,
            listOf("{player} scores {threshold}+ goals", "{player} scores {threshold}+ times", "{player} records {threshold}+ goals")
        ),
        EventCondition("F2", EventCategory.FORWARD, "player.assists", ComparisonType.GREATER_THAN, 2,
            listOf("{player} tallies {threshold}+ assists", "{player} earns {threshold}+ assists", "{player} assists {threshold}+ times")
        ),
        EventCondition("F3", EventCategory.FORWARD, "player.hits", ComparisonType.GREATER_THAN, 3,
            listOf("{player} delivers {threshold}+ hits", "{player} throws {threshold}+ body checks")
        ),
        EventCondition("F4", EventCategory.FORWARD, "player.sog", ComparisonType.GREATER_THAN, 4,
            listOf("{player} fires {threshold}+ shots on goal", "{player} puts {threshold}+ shots on net", "{player} shoots {threshold}+ times")
        ),
        EventCondition("F5", EventCategory.FORWARD, "player.toi", ComparisonType.GREATER_THAN, 20,
            listOf("{player} logs over {threshold} minutes of ice time", "{player} plays more than {threshold} minutes")
        ),

        // -------- DEFENSE --------
        EventCondition("D1", EventCategory.DEFENSE, "player.blockedShots", ComparisonType.GREATER_THAN, 2,
            listOf("{player} blocks {threshold}+ shots", "{player} gets {threshold}+ shots blocked")
        ),
        EventCondition("D2", EventCategory.DEFENSE, "player.hits", ComparisonType.GREATER_THAN, 3,
            listOf("{player} dishes out {threshold}+ hits", "{player} delivers {threshold}+ checks")
        ),
        EventCondition("D3", EventCategory.DEFENSE, "player.assists", ComparisonType.GREATER_THAN, 1,
            listOf("{player} contributes {threshold}+ assists", "{player} assists {threshold}+ goals", "{player} assists {threshold}+ times")
        ),
        EventCondition("D4", EventCategory.DEFENSE, "player.toi", ComparisonType.GREATER_THAN, 22,
            listOf("{player} plays more than {threshold} minutes", "{player} gets {threshold}+ minutes of ice time", "{player} logs over {threshold} minutes of ice time")
        ),

        // -------- GOALIES --------
        EventCondition("G1", EventCategory.GOALIE, "player.saves", ComparisonType.GREATER_THAN, 30,
            listOf("{player} makes {threshold}+ saves", "{player} stops {threshold}+ shots")
        ),
        EventCondition("G2", EventCategory.GOALIE, "player.savePct", ComparisonType.GREATER_THAN, 92,
            listOf("{player} posts a save percentage above {threshold}%", "{player} stops over {threshold}% of shots")
        ),
        EventCondition("G3", EventCategory.GOALIE, "player.goalsAgainst", ComparisonType.LESS_THAN, 2,
            listOf("{player} allows fewer than {threshold} goals", "{player} gives up under {threshold} goals")
        ),

        // -------- TEAM --------
        EventCondition("T1", EventCategory.TEAM, "team.goals", ComparisonType.GREATER_THAN, 3,
            listOf("{team} scores {threshold}+ goals", "{team} nets {threshold}+ goals in total")
        ),
        EventCondition("T2", EventCategory.TEAM, "team.sog", ComparisonType.GREATER_THAN, 30,
            listOf("{team} fires {threshold}+ shots on goal", "{team} registers {threshold}+ shots", "{team} puts {threshold}+ shots on net")
        ),
        EventCondition("T3", EventCategory.TEAM, "team.faceoffWinningPctg", ComparisonType.GREATER_THAN, 55,
            listOf("{team} wins over {threshold}% of faceoffs", "{team} controls {threshold}+% of faceoffs", "{team} wins more than {threshold}% of faceoffs")
        ),

        // -------- PENALTY --------
        EventCondition("P1", EventCategory.PENALTY, "team.penaltyMinutes", ComparisonType.GREATER_THAN, 10,
            listOf("{team} takes over {threshold} penalty minutes", "{team} spends more than {threshold} minutes in the box")
        )
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

        val homeForwards = homeRoster?.forwards ?: emptyList()
        val homeDefense = homeRoster?.defensemen ?: emptyList()
        val homeGoalies = homeRoster?.goalies ?: emptyList()

        val awayForwards = awayRoster?.forwards ?: emptyList()
        val awayDefense = awayRoster?.defensemen ?: emptyList()
        val awayGoalies = awayRoster?.goalies ?: emptyList()

        val homeTeamName = game.homeTeam.commonName.default
        val awayTeamName = game.awayTeam.commonName.default

        val eventsPerTeam = count / 2

        fun randomThresholdFor(subject: String): Int = when (subject) {
            "player.goals" -> (1..3).random(random)
            "player.assists" -> (1..3).random(random)
            "player.hits" -> (2..6).random(random)
            "player.sog" -> (3..7).random(random)
            "player.blockedShots" -> (1..4).random(random)
            "player.toi" -> (16..24).random(random)
            "player.saves" -> (20..40).random(random)
            "player.savePct" -> (89..95).random(random)
            "player.goalsAgainst" -> (1..3).random(random)
            "team.goals" -> (2..5).random(random)
            "team.sog" -> (20..35).random(random)
            "team.faceoffWinningPctg" -> (45..60).random(random)
            "team.penaltyMinutes" -> (4..10).random(random)
            else -> 1
        }

        fun generateTeamEvents(
            teamName: String,
            forwards: List<PlayerInfo>,
            defense: List<PlayerInfo>,
            goalies: List<PlayerInfo>
        ): List<EventCondition> {
            val templates = eventPool.shuffled(random).take(eventsPerTeam)
            return templates.map { template ->
                val threshold = randomThresholdFor(template.subject)
                val variant = template.labelTemplates.random(random)
                val playerName = when (template.category) {
                    EventCategory.FORWARD -> forwards.randomOrNull(random)?.fullName ?: "Forward"
                    EventCategory.DEFENSE -> defense.randomOrNull(random)?.fullName ?: "Defense"
                    EventCategory.GOALIE -> goalies.randomOrNull(random)?.fullName ?: "Goalie"
                    else -> teamName
                }

                val label = variant
                    .replace("{player}", playerName)
                    .replace("{team}", teamName)
                    .replace("{threshold}", threshold.toString())

                template.copy(labelTemplates = listOf(label), threshold = threshold)
            }
        }

        val homeEvents = generateTeamEvents(homeTeamName, homeForwards, homeDefense, homeGoalies)
        val awayEvents = generateTeamEvents(awayTeamName, awayForwards, awayDefense, awayGoalies)

        (homeEvents + awayEvents).shuffled(random)
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