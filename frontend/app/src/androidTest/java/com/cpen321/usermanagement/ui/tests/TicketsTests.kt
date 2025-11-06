package com.cpen321.usermanagement.ui.tests

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cpen321.usermanagement.data.remote.dto.Game
import com.cpen321.usermanagement.fakes.FakeAuthViewModel
import com.cpen321.usermanagement.fakes.FakeNhlDataManager
import com.cpen321.usermanagement.fakes.FakeTicketsRepository
import com.cpen321.usermanagement.ui.navigation.NavRoutes
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import com.cpen321.usermanagement.ui.screens.CreateBingoTicketScreen
import com.cpen321.usermanagement.ui.screens.TicketDetailScreen
import com.cpen321.usermanagement.ui.screens.TicketsScreen
import com.cpen321.usermanagement.ui.screens.TicketsScreenActions
import com.cpen321.usermanagement.ui.viewmodels.TicketsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TicketTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepo: FakeTicketsRepository
    private lateinit var fakeAuth: FakeAuthViewModel
    private lateinit var fakeNhl: FakeNhlDataManager
    private lateinit var navManager: NavigationStateManager
    private lateinit var viewModel: TicketsViewModel

    @Before
    fun setup() {
        fakeRepo = FakeTicketsRepository()
        fakeAuth = FakeAuthViewModel()
        fakeNhl = FakeNhlDataManager()
        navManager = NavigationStateManager()
        viewModel = TicketsViewModel(fakeRepo, navManager, fakeNhl)
    }

    private fun launchTicketsScreen() {
        composeTestRule.setContent {
            TicketsScreen(
                authViewModel = fakeAuth,
                ticketsViewModel = viewModel,
                actions = TicketsScreenActions(
                    onBackClick = {},
                    onCreateTicketClick = { navManager.navigateToCreateTicket() }
                )
            )
        }
        viewModel.loadTickets(fakeAuth.uiState.value.user!!._id)
    }

    @Test
    fun viewTickets_displaysListCorrectly() {
        launchTicketsScreen()

        composeTestRule.onNodeWithText("Bingo Tickets").assertIsDisplayed()
        composeTestRule.onNodeWithText("My First Ticket").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun deleteTicket_removesFromList() {
        launchTicketsScreen()

        composeTestRule.onNodeWithText("My First Ticket").assertIsDisplayed()

        composeTestRule.onNodeWithText("Delete").performClick()

        composeTestRule.waitUntilDoesNotExist(hasText("My First Ticket"))
    }

    @Test
    fun emptyState_showsNoTicketsText() {
        fakeRepo.emptyState = true
        launchTicketsScreen()

        composeTestRule.onNodeWithText("No bingo tickets yet.").assertIsDisplayed()
    }

    @Test
    fun createTicket_successfullyAddsToList() {
        // Navigate to Create Ticket screen
        navManager.navigateToCreateTicket()

        val userId = fakeAuth.uiState.value.user!!._id
        val game = fakeNhl.getGamesForTickets().first()

        // Simulate user entering ticket name and selecting events
        viewModel.createTicket(
            userId = userId,
            name = "Test Ticket",
            game = game,
            events = List(9) { "Event ${it + 1}" }
        )

        // Navigate back to TicketsScreen to verify it was added
        composeTestRule.setContent {
            TicketsScreen(
                authViewModel = fakeAuth,
                ticketsViewModel = viewModel,
                actions = TicketsScreenActions(
                    onBackClick = {},
                    onCreateTicketClick = { navManager.navigateToCreateTicket() }
                )
            )
        }

        composeTestRule.onNodeWithText("Test Ticket").assertIsDisplayed()
    }

    @Test
    fun toggleSquare_updatesCrossedOffState() = runBlocking {
        val ticket = fakeRepo.getTickets("currentUserId").getOrThrow().first()

        composeTestRule.setContent {
            TicketDetailScreen(
                ticket = ticket,
                onBackClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onAllNodesWithContentDescription("Bingo Square")[0].performClick()

        val updated = viewModel.uiState.value.allTickets.find { it._id == ticket._id }
        assert(updated?.crossedOff?.first() == true)
    }
}