package com.cpen321.usermanagement.data.local.preferences

import android.util.Log
import com.cpen321.usermanagement.data.remote.dto.Boxscore
import com.cpen321.usermanagement.data.remote.dto.Game
import com.cpen321.usermanagement.data.remote.dto.GameDay
import com.cpen321.usermanagement.data.repository.NHLRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import com.cpen321.usermanagement.data.remote.dto.PlayerInfo
import com.cpen321.usermanagement.data.remote.dto.PlayerStats
import com.cpen321.usermanagement.data.remote.dto.GoalieStats
import com.cpen321.usermanagement.data.remote.dto.TeamInfo
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
    val subject: String,         // e.g. "goals", "assists", "penaltyMinutes"
    val comparison: ComparisonType,
    val threshold: Int,
    val teamAbbrev: String? = null,  // e.g. "VAN" or "BOS"
    val playerId: Long? = null,       // optional for player-specific events
    val playerName: String? = null,  // cached for label generation
)


/**
 * Shared manager for NHL schedule data across the app
 * Maintains a single source of truth for game schedule
 */
@Singleton
class NhlDataManagerImpl @Inject constructor(
    private val nhlRepository: NHLRepository
) : NhlDataManager {
    companion object {
        private const val TAG = "NhlDataManager"
    }

    private val _uiState = MutableStateFlow(NhlDataState())
    override val uiState: StateFlow<NhlDataState> = _uiState.asStateFlow()

    private val eventPool = listOf(
        // -------- FORWARDS --------
        EventCondition("F1", EventCategory.FORWARD, "player.goals", ComparisonType.GREATER_THAN, 2,
            "{player} scores {threshold}+ goals"
        ),
        EventCondition("F2", EventCategory.FORWARD, "player.assists", ComparisonType.GREATER_THAN, 2,
            "{player} gets {threshold}+ assists"
        ),
        EventCondition("F3", EventCategory.FORWARD, "player.hits", ComparisonType.GREATER_THAN, 3,
            "{player} delivers {threshold}+ hits"
        ),
        EventCondition("F4", EventCategory.FORWARD, "player.sog", ComparisonType.GREATER_THAN, 4,
            "{player} shoots {threshold}+ times"
        ),
        EventCondition("F5", EventCategory.FORWARD, "player.toi", ComparisonType.GREATER_THAN, 20,
            "{player} logs over {threshold} minutes of ice time"
        ),

        // -------- DEFENSE --------
        EventCondition("D1", EventCategory.DEFENSE, "player.blockedShots", ComparisonType.GREATER_THAN, 2,
            "{player} blocks {threshold}+ shots"
        ),
        EventCondition("D2", EventCategory.DEFENSE, "player.hits", ComparisonType.GREATER_THAN, 3,
            "{player} dishes out {threshold}+ hits"
        ),
        EventCondition("D3", EventCategory.DEFENSE, "player.assists", ComparisonType.GREATER_THAN, 1,
            "{player} assists {threshold}+ times"
        ),
        EventCondition("D4", EventCategory.DEFENSE, "player.toi", ComparisonType.GREATER_THAN, 22,
            "{player} gets {threshold}+ minutes of ice time"
        ),

        // -------- GOALIES --------
        EventCondition("G1", EventCategory.GOALIE, "player.saves", ComparisonType.GREATER_THAN, 30,
            "{player} makes {threshold}+ saves"
        ),

        // -------- TEAM --------
        EventCondition("T1", EventCategory.TEAM, "team.goals", ComparisonType.GREATER_THAN, 3,
            "{team} scores {threshold}+ goals"
        ),
        EventCondition("T2", EventCategory.TEAM, "team.sog", ComparisonType.GREATER_THAN, 30,
            "{team} registers {threshold}+ shots on goal"
        ),

        // -------- PENALTY --------
        EventCondition("P1", EventCategory.PENALTY, "team.penaltyMinutes", ComparisonType.GREATER_THAN, 10,
            "{team} takes over {threshold} penalty minutes"
        )
    )

    /**
     * Load current NHL schedule
     */
    override suspend fun loadSchedule(): Result<List<GameDay>> {
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
    override fun getUpcomingGames(): List<Game> {
        return _uiState.value.gameSchedule?.flatMap { gameWeek ->
            gameWeek.games.filter { game ->
                game.gameState == "FUT" // Future games only
            }
        } ?: emptyList()
    }

    /**
     * Get games for challenge creation (upcoming games only)
     */
    override fun getGamesForChallenges(): List<Game> {
        return getUpcomingGames().take(20) // Limit to next 20 games
    }

    /**
     * Get games for ticket creation
     */
    override fun getGamesForTickets(): List<Game> {
        return getUpcomingGames().take(20)
    }

    override suspend fun getEventsForGame(gameId: Long, count: Int): List<EventCondition> = coroutineScope {
        val game = getGameById(gameId) ?: return@coroutineScope emptyList()
        val random = Random(gameId)

        val homeRosterDeferred = async { nhlRepository.getTeamRoster(game.homeTeam.abbrev).getOrNull() }
        val awayRosterDeferred = async { nhlRepository.getTeamRoster(game.awayTeam.abbrev).getOrNull() }

        val homeRoster = homeRosterDeferred.await()
        val awayRoster = awayRosterDeferred.await()

        val allPlayers = (homeRoster?.forwards ?: emptyList()) +
                (homeRoster?.defensemen ?: emptyList()) +
                (awayRoster?.forwards ?: emptyList()) +
                (awayRoster?.defensemen ?: emptyList())

        val allGoalies = (homeRoster?.goalies ?: emptyList()) +
                (awayRoster?.goalies ?: emptyList())

        val subjects = listOf("goals", "assists", "hits", "sog", "blockedShots", "toi")

        val events = mutableListOf<EventCondition>()

        repeat(count) {
            val subject = subjects.random(random)
            val comparison = ComparisonType.GREATER_THAN
            val threshold = randomThresholdFor(subject)

            val isTeamEvent = subject in listOf("penaltyMinutes", "goals", "sog")
                    && random.nextDouble() < 0.2  // 20% chance for team event

            if (isTeamEvent) {
                val team = if (random.nextBoolean()) game.homeTeam.abbrev else game.awayTeam.abbrev
                events += EventCondition(
                    id = "T${it}_${team}",
                    category = EventCategory.TEAM,
                    subject = subject,
                    comparison = comparison,
                    threshold = threshold,
                    teamAbbrev = team
                )
            } else {
                val player = allPlayers.random(random)
                val category = when (player.positionCode) {
                    "G" -> EventCategory.GOALIE
                    "D" -> EventCategory.DEFENSE
                    else -> EventCategory.FORWARD
                }

                events += EventCondition(
                    id = "P${it}_${player.id}",
                    category = category,
                    subject = subject,
                    comparison = comparison,
                    threshold = threshold,
                    playerId = player.id,
                    playerName = player.fullName,
                    teamAbbrev = null
                )
            }
        }

        return@coroutineScope events
    }

    /**
     * Get a specific game by ID
     */
    override fun getGameById(gameId: Long): Game? {
        return _uiState.value.gameSchedule?.flatMap { it.games }?.find { it.id == gameId }
    }

    /**
     * Clear error state
     */
    override fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Refresh the schedule data
     */
    override suspend fun refreshSchedule(): Result<List<GameDay>> {
        return loadSchedule()
    }

    /**
     * Get boxscore for a specific game
     */
    override suspend fun getBoxscore(gameId: Long): Result<Boxscore> {
        return nhlRepository.getBoxscore(gameId)
    }

    /**
     * Return true if the given event condition is satisfied by the boxscore.
     * Handles player/goalie/team categories and subjects.
     */
    override suspend fun isFulfilled(event: EventCondition, boxscore: Boxscore): Boolean {
        // Allow subjects that are either "goals" or "player.goals" etc.
        val normalizedSubject = event.subject.removePrefix("player.").removePrefix("team.").removePrefix("goalie.")

        // Try to fetch value according to category and provided ids/names
        val value = when (event.category) {
            EventCategory.FORWARD, EventCategory.DEFENSE -> {
                // Player stat (for skaters)
                getValueForField(boxscore, event.copy(subject = normalizedSubject, playerName = event.playerName, playerId = event.playerId, teamAbbrev = event.teamAbbrev))
            }

            EventCategory.GOALIE -> {
                // Goalie stat (saves, goalsAgainst, toi, etc.)
                getValueForField(boxscore, event.copy(subject = normalizedSubject, playerName = event.playerName, playerId = event.playerId, teamAbbrev = event.teamAbbrev))
            }

            EventCategory.TEAM, EventCategory.PENALTY -> {
                // Team-level stat
                getValueForField(boxscore, event.copy(subject = normalizedSubject))
            }
        }

        // If value missing, treat as not fulfilled
        if (value == null) return false

        return compareStat(value, event.threshold, event.comparison)
    }

    override fun formatEventLabel(event: EventCondition): String {
        val subject = when {
            event.playerName != null -> event.playerName
            event.teamAbbrev != null -> event.teamAbbrev
            else -> "Player"
        }

        val statName = when (event.subject) {
            "player.goals" -> "scores"
            "player.assists" -> "assists"
            "player.hits" -> "makes hits"
            "player.sog" -> "takes shots"
            "player.blockedShots" -> "blocks shots"
            "goalie.saves" -> "makes saves"
            "team.goals" -> "scores goals"
            "team.penalties" -> "takes penalties"
            "team.shots" -> "takes shots"
            else -> event.subject.replaceFirstChar { it.uppercase() } // fallback
        }

        val comparison = when (event.comparison) {
            ComparisonType.GREATER_THAN -> "${event.threshold}+"
            ComparisonType.LESS_THAN -> "< ${event.threshold}"
        }

        return "$subject $statName ($comparison)"
    }

    /**
     * Get an integer value for the event's subject from the boxscore.
     * Accepts subject values like "goals", "sog", "saves", "penaltyMinutes", "toi".
     * Returns null if the requested stat isn't available for the given event.
     */
    private fun getValueForField(boxscore: Boxscore, eventCondition: EventCondition): Int? {
        val subject = eventCondition.subject.removePrefix("player.").removePrefix("team.").removePrefix("goalie.")

        // TEAM-level queries (sums, or team fields)
        if (eventCondition.teamAbbrev != null || subject.startsWith("team") || eventCondition.category == EventCategory.TEAM || eventCondition.category == EventCategory.PENALTY) {
            val teamAbbrev = eventCondition.teamAbbrev ?: return null
            val isHome = boxscore.homeTeam.abbrev == teamAbbrev
            val teamInfo = if (isHome) boxscore.homeTeam else boxscore.awayTeam
            val teamPlayers = if (isHome) boxscore.playerByGameStats.homeTeam else boxscore.playerByGameStats.awayTeam

            return when (subject) {
                "goals" -> teamInfo.score
                "sog" -> teamInfo.sog
                // No direct team PIM field: sum player pims
                "penaltyMinutes", "pim", "penalties" -> {
                    val allPlayers = teamPlayers.forwards + teamPlayers.defense
                    allPlayers.sumOf { it.pim ?: 0 }
                }
                else -> null
            }
        }

        // PLAYER-level (skaters) OR GOALIE-level: prefer lookup by playerId if present
        val playerId = eventCondition.playerId
        if (playerId != null) {
            // Try skaters first
            val skater = getSkaterById(boxscore, playerId)
            if (skater != null) {
                return getSkaterStatBySubject(skater, subject)
            }

            // Try goalie table if not found among skaters
            val goalie = getGoalieById(boxscore, playerId)
            if (goalie != null) {
                return getGoalieStatBySubject(goalie, subject)
            }

            return null
        }

        // If no playerId, try by playerName (case-insensitive)
        val playerName = eventCondition.playerName
        if (!playerName.isNullOrBlank()) {
            val skater = findPlayerByName(boxscore, playerName)
            if (skater != null) {
                return getSkaterStatBySubject(skater, subject)
            }
            val goalie = findGoalieByName(boxscore, playerName)
            if (goalie != null) {
                return getGoalieStatBySubject(goalie, subject)
            }
        }

        // Nothing found
        return null
    }

    /**
     * Get skater (non-goalie) stat by subject.
     */
    private fun getSkaterStatBySubject(player: PlayerStats, subject: String): Int? {
        return when (subject) {
            "goals" -> player.goals
            "assists" -> player.assists
            "points" -> player.points
            "hits" -> player.hits
            "sog" -> player.sog
            "blockedShots" -> player.blockedShots
            "pim" -> player.pim ?: 0
            "toi" -> parseToiToMinutes(player.toi)
            else -> null
        }
    }

    /**
     * Get goalie stat by subject.
     * Note: goalie stats naming in boxscore used "saves", "goalsAgainst", "savePctg" etc.
     */
    private fun getGoalieStatBySubject(goalie: GoalieStats, subject: String): Int? {
        return when (subject) {
            "saves" -> goalie.saves
            "goalsAgainst", "ga" -> goalie.goalsAgainst
            // Convert TOI string to minutes for goalie too
            "toi" -> parseToiToMinutes(goalie.toi)
            // If you want to support save percentage comparisons you'll need to adapt return type (Double)
            else -> null
        }
    }

    /**
     * Helper: find skater by numeric player id in both teams
     */
    private fun getSkaterById(boxscore: Boxscore, id: Long): PlayerStats? {
        return (boxscore.playerByGameStats.homeTeam.forwards + boxscore.playerByGameStats.homeTeam.defense +
                boxscore.playerByGameStats.awayTeam.forwards + boxscore.playerByGameStats.awayTeam.defense)
            .firstOrNull { it.playerId == id }
    }

    /**
     * Helper: find goalie by numeric player id in both teams
     */
    private fun getGoalieById(boxscore: Boxscore, id: Long): GoalieStats? {
        return (boxscore.playerByGameStats.homeTeam.goalies + boxscore.playerByGameStats.awayTeam.goalies)
            .firstOrNull { it.playerId == id }
    }

    /**
     * Find a skater by display name (case-insensitive). Uses PlayerStats.name.default
     */
    private fun findPlayerByName(boxscore: Boxscore, name: String): PlayerStats? {
        val allPlayers = boxscore.playerByGameStats.awayTeam.forwards +
                boxscore.playerByGameStats.awayTeam.defense +
                boxscore.playerByGameStats.homeTeam.forwards +
                boxscore.playerByGameStats.homeTeam.defense

        return allPlayers.firstOrNull { it.name.default.equals(name, ignoreCase = true) }
    }

    /**
     * Find a goalie by display name (case-insensitive). Uses GoalieStats.name.default
     */
    private fun findGoalieByName(boxscore: Boxscore, name: String): GoalieStats? {
        val allGoalies = boxscore.playerByGameStats.awayTeam.goalies +
                boxscore.playerByGameStats.homeTeam.goalies

        return allGoalies.firstOrNull { it.name.default.equals(name, ignoreCase = true) }
    }

    /**
     * Parse TOI "MM:SS" (or "H:MM:SS") into minutes (rounded down).
     * Returns 0 on parse failure.
     */
    private fun parseToiToMinutes(toi: String?): Int {
        if (toi.isNullOrBlank()) return 0
        // Some goalies may have "HH:MM:SS" while skaters usually "MM:SS"
        val parts = toi.split(":").mapNotNull { it.toIntOrNull() }
        if (parts.isEmpty()) return 0
        return when (parts.size) {
            2 -> { // MM:SS
                val minutes = parts[0]
                val seconds = parts[1]
                minutes + (seconds / 60)
            }
            3 -> { // H:MM:SS
                val hours = parts[0]
                val minutes = parts[1]
                val seconds = parts[2]
                hours * 60 + minutes + (seconds / 60)
            }
            else -> 0
        }
    }

    /**
     * Compare integer stat with threshold using ComparisonType.
     */
    private fun compareStat(value: Int, threshold: Int, comparison: ComparisonType): Boolean {
        return when (comparison) {
            ComparisonType.GREATER_THAN -> value > threshold
            ComparisonType.LESS_THAN -> value < threshold
        }
    }

    /**
     * Produce a random, reasonable threshold for a given subject.
     * Tuned to typical hockey stat ranges.
     */
    private fun randomThresholdFor(subject: String): Int {
        return when (subject.removePrefix("player.").removePrefix("team.").removePrefix("goalie.")) {
            "goals" -> (1..2).random()
            "assists" -> (1..2).random()
            "hits" -> (2..6).random()
            "sog" -> (2..7).random()
            "blockedShots" -> (1..4).random()
            "toi" -> (12..25).random() // minutes
            "saves" -> (20..40).random()
            "goalsAgainst" -> (0..3).random()
            "penaltyMinutes", "pim" -> (2..12).random()
            else -> (1..3).random()
        }
    }
}