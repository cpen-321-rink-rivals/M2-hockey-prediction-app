package com.cpen321.usermanagement.ui.screens

import Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.local.preferences.EventCondition
import com.cpen321.usermanagement.data.local.preferences.NhlDataManager
import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import com.cpen321.usermanagement.data.remote.dto.TicketsUiState
import com.cpen321.usermanagement.ui.components.TeamLogo
import com.cpen321.usermanagement.ui.viewmodels.AuthViewModel
import com.cpen321.usermanagement.ui.viewmodels.AuthViewModelContract
import com.cpen321.usermanagement.ui.viewmodels.TicketsViewModel

data class TicketsScreenActions(
    val onBackClick: () -> Unit,
    val onCreateTicketClick: () -> Unit
)

private data class TicketsScreenCallbacks(
    val onBackClick: () -> Unit,
    val onCreateTicketClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(
    authViewModel: AuthViewModelContract,
    actions: TicketsScreenActions,
    ticketsViewModel: TicketsViewModel
) {
    val uiState by ticketsViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val userId = authState.user?._id ?: ""

    // Side effects: clear messages and load tickets
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            ticketsViewModel.clearSuccessMessage()
            ticketsViewModel.clearError()
            ticketsViewModel.loadTickets(userId)
        }
    }

    TicketsContent(
        uiState = uiState,
        ticketsViewModel = ticketsViewModel,
        callbacks = TicketsScreenCallbacks(
            onBackClick = actions.onBackClick,
            onCreateTicketClick = actions.onCreateTicketClick
        )
    )
}

@Composable
private fun TicketsContent(
    modifier: Modifier = Modifier,
    uiState: TicketsUiState,
    ticketsViewModel: TicketsViewModel,
    callbacks: TicketsScreenCallbacks
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TicketsTopBar(onBackClick = { callbacks.onBackClick() })
        }
    ) { paddingValues ->
        TicketsBody(
            paddingValues = paddingValues,
            allTickets = uiState.allTickets,
            isLoading = uiState.isLoadingTickets,
            ticketsViewModel = ticketsViewModel,
            onCreateTicketClick = callbacks.onCreateTicketClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.bingo_tickets),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun TicketsBody(
    paddingValues: PaddingValues,
    allTickets: List<BingoTicket>,
    isLoading: Boolean,
    ticketsViewModel: TicketsViewModel,
    onCreateTicketClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            else -> {
                TicketsList(
                    modifier = Modifier.fillMaxSize(),
                    allTickets = allTickets,
                    ticketsViewModel = ticketsViewModel
                )
                AddTicketButton(
                    onClick = onCreateTicketClick,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .width(200.dp)
                        .height(60.dp)
                )
            }
        }
    }
}

@Composable
fun TicketsList(
    modifier: Modifier = Modifier,
    allTickets: List<BingoTicket> = emptyList(),
    ticketsViewModel: TicketsViewModel
) {
    if (allTickets.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No bingo tickets yet.")
        }
    } else {
        LazyColumn(
            modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(
                count = allTickets.size,
                key = { index -> allTickets[index]._id }
            ) { index ->
                val ticket = allTickets[index]

                // Use reusable ticket card (shows grid and total score)
                com.cpen321.usermanagement.ui.components.BingoTicketCard(
                    ticket = ticket,
                    nhlDataManager = ticketsViewModel.nhlDataManager,
                    onDelete = { ticketsViewModel.deleteTicket(ticket._id) },
                    onClick = { ticketsViewModel.selectTicket(ticket._id) },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


@Composable
private fun AddTicketButton(
    onClick: () -> Unit,
    modifier: Modifier
) {
    Button(onClick = onClick, modifier = modifier) {
        Text("New Bingo Ticket")
    }
}

@Composable
fun BingoGridPreview(
    events: List<EventCondition>,
    crossedOff: List<Boolean>? = null,
    nhlDataManager: NhlDataManager
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (row in 0 until 3) {
            Row(horizontalArrangement = Arrangement.Center) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    val isCrossed = crossedOff?.getOrNull(index) ?: false
                    val event = events.getOrNull(index)

                    // Use a remembered coroutine scope to call suspend formatter
                    val label = remember(event) { mutableStateOf("") }
                    LaunchedEffect(event) {
                        if (event != null) {
                            label.value = nhlDataManager.formatEventLabel(event)
                        }
                    }

                    // Get team logo if this is a team-specific event
                    val teamAbbrev = event?.teamAbbrev
                    val logoUrl = remember(teamAbbrev) {
                        teamAbbrev?.let { abbrev ->
                            nhlDataManager.uiState.value.gameSchedule
                                ?.flatMap { it.games }
                                ?.flatMap { listOf(it.homeTeam, it.awayTeam) }
                                ?.find { it.abbrev == abbrev }
                                ?.logo
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(80.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .background(
                                if (isCrossed)
                                    Color.Green.copy(alpha = 0.4f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!logoUrl.isNullOrBlank()) {
                            // Show logo with label for team events
                            TeamLogo(
                                logoUrl = logoUrl,
                                teamAbbrev = label.value,
                                size = 32.dp,
                                showAbbrev = true,
                                abbrevFontSize = 9.sp,
                                abbrevColor = if (isCrossed)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            // Show text only for non-team events
                            Text(
                                text = label.value,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(6.dp),
                                color = if (isCrossed)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}