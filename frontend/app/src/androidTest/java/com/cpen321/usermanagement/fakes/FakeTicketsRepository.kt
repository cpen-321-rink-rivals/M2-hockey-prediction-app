package com.cpen321.usermanagement.fakes

import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import com.cpen321.usermanagement.data.remote.dto.Game
import com.cpen321.usermanagement.data.repository.TicketsRepository
import kotlinx.coroutines.delay

class FakeTicketsRepository : TicketsRepository {

    var emptyState = false

    private val fakeGames = listOf(
        Game(
            id = 1L,
            season = 2025,
            gameType = 2,
            venue = com.cpen321.usermanagement.data.remote.dto.Venue("Rogers Arena"),
            neutralSite = false,
            startTimeUTC = "2025-11-10T02:00:00Z",
            easternUTCOffset = "-05:00",
            venueUTCOffset = "-08:00",
            venueTimezone = "PST",
            gameState = "Scheduled",
            gameScheduleState = "OK",
            tvBroadcasts = emptyList(),
            awayTeam = com.cpen321.usermanagement.data.remote.dto.Team(
                id = 10,
                commonName = com.cpen321.usermanagement.data.remote.dto.Name("Maple Leafs"),
                placeName = com.cpen321.usermanagement.data.remote.dto.Name("Toronto"),
                placeNameWithPreposition = com.cpen321.usermanagement.data.remote.dto.Name("Toronto"),
                abbrev = "TOR",
                logo = "",
                darkLogo = "",
                radioLink = ""
            ),
            homeTeam = com.cpen321.usermanagement.data.remote.dto.Team(
                id = 22,
                commonName = com.cpen321.usermanagement.data.remote.dto.Name("Canucks"),
                placeName = com.cpen321.usermanagement.data.remote.dto.Name("Vancouver"),
                placeNameWithPreposition = com.cpen321.usermanagement.data.remote.dto.Name("Vancouver"),
                abbrev = "VAN",
                logo = "",
                darkLogo = "",
                radioLink = ""
            ),
            periodDescriptor = com.cpen321.usermanagement.data.remote.dto.PeriodDescriptor(3, "REG", 3),
            ticketsLink = null,
            ticketsLinkFr = null,
            gameCenterLink = null
        )
    )

    private val fakeTickets = mutableListOf(
        BingoTicket(
            _id = "t1",
            userId = "currentUserId",
            name = "My First Ticket",
            game = fakeGames.first(),
            events = List(9) { "Event $it" },
            crossedOff = List(9) { false }
        )
    )

    override suspend fun getTickets(userId: String): Result<List<BingoTicket>> {
        delay(100)
        return if (emptyState) Result.success(emptyList())
        else Result.success(fakeTickets.toList())
    }

    override suspend fun getTicketById(ticketId: String): Result<BingoTicket> {
        return fakeTickets.find { it._id == ticketId }?.let { Result.success(it) }
            ?: Result.failure(Exception("Ticket not found"))
    }

    override suspend fun createTicket(
        userId: String,
        name: String,
        game: Game,
        events: List<String>
    ): Result<BingoTicket> {
        delay(100)
        val newTicket = BingoTicket(
            _id = "t${fakeTickets.size + 1}",
            userId = userId,
            name = name,
            game = game,
            events = events
        )
        fakeTickets.add(newTicket)
        return Result.success(newTicket)
    }

    override suspend fun deleteTicket(ticketId: String): Result<Unit> {
        fakeTickets.removeAll { it._id == ticketId }
        return Result.success(Unit)
    }

    override suspend fun updateCrossedOff(
        ticketId: String,
        crossedOff: List<Boolean>
    ): Result<BingoTicket> {
        val index = fakeTickets.indexOfFirst { it._id == ticketId }
        if (index == -1) return Result.failure(Exception("Ticket not found"))
        val updated = fakeTickets[index].copy(crossedOff = crossedOff)
        fakeTickets[index] = updated
        return Result.success(updated)
    }

    fun getFakeGames(): List<Game> = fakeGames
}