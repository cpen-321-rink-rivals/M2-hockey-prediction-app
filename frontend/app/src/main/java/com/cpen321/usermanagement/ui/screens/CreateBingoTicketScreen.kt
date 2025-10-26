package com.cpen321.usermanagement.ui.screens

import Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.local.preferences.NhlDataManager
import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import com.cpen321.usermanagement.data.remote.dto.Game
import com.cpen321.usermanagement.data.remote.dto.Name
import com.cpen321.usermanagement.data.remote.dto.PeriodDescriptor
import com.cpen321.usermanagement.data.remote.dto.Team
import com.cpen321.usermanagement.data.remote.dto.TvBroadcast
import com.cpen321.usermanagement.data.remote.dto.Venue
import com.cpen321.usermanagement.ui.viewmodels.AuthViewModel
import com.cpen321.usermanagement.ui.viewmodels.TicketsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBingoTicketScreen(
    ticketsViewModel: TicketsViewModel,
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onTicketCreated: () -> Unit
) {


    val uiState by ticketsViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val userId = authState.user?._id ?: ""

    var ticketName by remember { mutableStateOf("") }  // Ticket name state
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var availableEvents by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedEvents by remember { mutableStateOf(List(9) { "" }) }
    var showEventPickerForIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        ticketsViewModel.loadUpcomingGames()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Bingo Ticket") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(name = R.drawable.ic_arrow_back)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🆕 Step 0: Enter Ticket Name
            OutlinedTextField(
                value = ticketName,
                onValueChange = { ticketName = it },
                label = { Text("Ticket Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1: Select Game
            Text("Select an upcoming game:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.isLoadingGames) {
                CircularProgressIndicator()
            } else if (uiState.availableGames.isEmpty()) {
                Text("No upcoming games found.")
            } else {
                GameDropdown(
                    games = uiState.availableGames,
                    selectedGame = selectedGame,
                    onGameSelected = { game ->
                        selectedGame = game
                        ticketsViewModel.getEventsForGame(game.id) { events ->
                            availableEvents = events
                        }
                        selectedEvents = List(9) { "" }
                    }
                )
            }


            Spacer(modifier = Modifier.height(24.dp))

            // Step 2: Bingo Grid
            if (selectedGame != null) {
                Text("Fill your bingo ticket:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                BingoGrid(
                    selectedEvents = selectedEvents,
                    onSquareClick = { index -> showEventPickerForIndex = index },
                    onRemoveEvent = { index ->
                        selectedEvents = selectedEvents.toMutableList().also { it[index] = "" }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (userId.isNotBlank()) {
                            ticketsViewModel.createTicket(
                                userId = userId,
                                name = ticketName,
                                game = selectedGame!!,
                                events = selectedEvents
                            )
                        }
                    },
                    enabled = !uiState.isCreating && userId.isNotBlank() && ticketName.isNotBlank() && selectedEvents.none { it.isBlank() },
                ) {
                    Text("Save Bingo Ticket")
                }
            }
        }

        // Event Picker Dialog
        showEventPickerForIndex?.let { index ->
            EventPickerDialog(
                allEvents = availableEvents,
                selectedEvents = selectedEvents,
                onDismiss = { showEventPickerForIndex = null },
                onEventSelected = { chosen ->
                    selectedEvents = selectedEvents.toMutableList().also { it[index] = chosen }
                    showEventPickerForIndex = null
                }
            )
        }
    }
}

@Composable
private fun GameDropdown(
    games: List<Game>,
    selectedGame: Game?,
    onGameSelected: (Game) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedGame?.let { "${it.awayTeam.abbrev} vs ${it.homeTeam.abbrev}" } ?: "",
            onValueChange = {},
            label = { Text("Select game") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            enabled = false,
            readOnly = true
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            games.forEach { game ->
                DropdownMenuItem(
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "${game.awayTeam.abbrev} vs ${game.homeTeam.abbrev}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatDateTime(game.startTimeUTC),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onGameSelected(game)
                        expanded = false
                    }
                )

            }
        }
    }
}

@Composable
private fun BingoGrid(
    selectedEvents: List<String>,
    onSquareClick: (Int) -> Unit,
    onRemoveEvent: (Int) -> Unit
) {
    Column {
        for (row in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    BingoSquare(
                        eventText = selectedEvents[index],
                        onClick = { onSquareClick(index) },
                        onRemove = { onRemoveEvent(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BingoSquare(
    eventText: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(100.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (eventText.isBlank()) {
            Text("Tap to add", style = MaterialTheme.typography.bodyMedium)
        } else {
            Box(Modifier.fillMaxSize()) {
                Text(
                    text = eventText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(6.dp)
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                ) {
                    Icon(name = R.drawable.ic_delete_forever)
                }
            }
        }
    }
}

@Composable
private fun EventPickerDialog(
    allEvents: List<String>,
    selectedEvents: List<String>,
    onDismiss: () -> Unit,
    onEventSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            LazyColumn {
                val available = allEvents.filterNot { it in selectedEvents }
                items(available.size) { index ->
                    val event = available[index]
                    Text(
                        text = event,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onEventSelected(event)
                            }
                            .padding(8.dp)
                    )
                }
            }
        },
        title = { Text("Select an Event") }
    )
}
