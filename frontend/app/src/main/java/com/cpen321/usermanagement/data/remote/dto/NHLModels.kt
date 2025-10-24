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