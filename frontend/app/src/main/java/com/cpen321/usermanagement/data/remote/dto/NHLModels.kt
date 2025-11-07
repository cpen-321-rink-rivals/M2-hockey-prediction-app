package com.cpen321.usermanagement.data.remote.dto



data class ScheduleResponse(
    val nextStartDate: String,
    val previousStartDate: String,
    val gameWeek: List<GameDay>
)

data class GameDay(
    val date: String,
    val dayAbbrev: String,
    val numberOfGames: Int,
    val datePromo: List<Any>,
    val games: List<Game>
)

data class Game(
    val id: Long,
    val season: Long,
    val gameType: Int,
    val venue: Venue,
    val neutralSite: Boolean,
    val startTimeUTC: String,
    val easternUTCOffset: String,
    val venueUTCOffset: String,
    val venueTimezone: String,
    val gameState: String,
    val gameScheduleState: String,
    val tvBroadcasts: List<TvBroadcast>,
    val awayTeam: Team,
    val homeTeam: Team,
    val periodDescriptor: PeriodDescriptor,
    val ticketsLink: String?,
    val ticketsLinkFr: String?,
    val gameCenterLink: String?
)

data class Venue(
    val default: String
    // Add "fr" if present in your data
)

data class TvBroadcast(
    val id: Long,
    val market: String,
    val countryCode: String,
    val network: String,
    val sequenceNumber: Int
)

data class Team(
    val id: Long,
    val commonName: Name,
    val placeName: Name,
    val placeNameWithPreposition: Name,
    val abbrev: String,
    val logo: String,
    val darkLogo: String,
    val awaySplitSquad: Boolean? = null,
    val homeSplitSquad: Boolean? = null,
    val radioLink: String,
    val odds: List<Odds>? = null
)

data class Name(
    val default: String,
    val fr: String? = null
)

data class Odds(
    val providerId: Int,
    val value: String
)

data class PeriodDescriptor(
    val number: Int,
    val periodType: String,
    val maxRegulationPeriods: Int
)

data class TeamRosterResponse(
    val forwards: List<PlayerInfo> = emptyList(),
    val defensemen: List<PlayerInfo> = emptyList(),
    val goalies: List<PlayerInfo> = emptyList()
)

data class PlayerInfo(
    val id: Long,
    val headshot: String?,
    val firstName: NameField,
    val lastName: NameField,
    val sweaterNumber: Int?,
    val positionCode: String,
    val shootsCatches: String?,
    val heightInInches: Int?,
    val weightInPounds: Int?,
    val birthDate: String?,
    val birthCity: NameField?,
    val birthCountry: String?,
    val birthStateProvince: NameField?
) {
    val fullName: String get() = "${firstName.default} ${lastName.default}"
}

data class NameField(
    val default: String
)

data class BoxscoreResponse(
    val gameId: Long,
    val homeTeam: TeamStatsData,
    val awayTeam: TeamStatsData
)

data class TeamStatsData(
    val team: TeamInfo,
    val teamStats: TeamStatLine,
    val players: Map<String, PlayerBoxscore>,
    val goalies: Map<String, GoalieBoxscore>
)

data class TeamInfo(
    val id: Long,
    val abbrev: String,
    val name: String
)

data class TeamStatLine(
    val goals: Int,
    val assists: Int,
    val shots: Int,
    val hits: Int,
    val blockedShots: Int,
    val pim: Int
)

data class PlayerBoxscore(
    val playerId: Long,
    val firstName: String,
    val lastName: String,
    val stats: PlayerStats
)

data class PlayerStats(
    val goals: Int,
    val assists: Int,
    val shots: Int,
    val hits: Int,
    val blockedShots: Int,
    val timeOnIce: String
)

data class GoalieBoxscore(
    val playerId: Long,
    val firstName: String,
    val lastName: String,
    val stats: GoalieStats
)

data class GoalieStats(
    val saves: Int,
    val shotsAgainst: Int,
    val goalsAgainst: Int
)
