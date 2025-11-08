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



// Boxscore data

data class Boxscore(
    val id: Long,
    val season: Int,
    val gameType: Int,
    val gameDate: String,
    val gameState: String,
    val limitedScoring: Boolean,
    val periodDescriptor: PeriodDescriptor,
    val awayTeam: TeamInfo,
    val homeTeam: TeamInfo,
    val playerByGameStats: PlayerByGameStats
)

data class TeamInfo(
    val id: Int,
    val abbrev: String,
    val score: Int,
    val sog: Int, // Shots on Goal
    val commonName: NameWrapper,
    val placeName: NameWrapper,
    val logo: String,
    val darkLogo: String
)

data class NameWrapper(
    val default: String
)

data class PlayerByGameStats(
    val awayTeam: TeamPlayers,
    val homeTeam: TeamPlayers
)

data class TeamPlayers(
    val forwards: List<PlayerStats> = emptyList(),
    val defense: List<PlayerStats> = emptyList(),
    val goalies: List<GoalieStats> = emptyList()
)

data class PlayerStats(
    val playerId: Long,
    val sweaterNumber: Int,
    val name: NameWrapper,
    val position: String,
    val goals: Int,
    val assists: Int,
    val points: Int,
    val plusMinus: Int,
    val pim: Int,
    val hits: Int,
    val powerPlayGoals: Int,
    val sog: Int,
    val faceoffWinningPctg: Double?,
    val toi: String, // Time on Ice — can be parsed into seconds if needed
    val blockedShots: Int,
    val shifts: Int,
    val giveaways: Int,
    val takeaways: Int
)

data class GoalieStats(
    val playerId: Long,
    val sweaterNumber: Int,
    val name: NameWrapper,
    val position: String,
    val evenStrengthShotsAgainst: Int,
    val powerPlayShotsAgainst: Int,
    val shorthandedShotsAgainst: Int,
    val saveShotsAgainst: Int,
    val evenStrengthGoalsAgainst: Int,
    val powerPlayGoalsAgainst: Int,
    val shorthandedGoalsAgainst: Int,
    val pim: Int,
    val goalsAgainst: Int,
    val toi: String,
    val shotsAgainst: Int,
    val saves: Int
)
