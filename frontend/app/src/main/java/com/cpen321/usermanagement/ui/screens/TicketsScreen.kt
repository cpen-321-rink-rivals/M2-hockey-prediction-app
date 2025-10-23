package com.cpen321.usermanagement.ui.screens

import Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import com.cpen321.usermanagement.data.remote.dto.TicketsUiState
import com.cpen321.usermanagement.ui.viewmodels.AuthViewModel
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
    authViewModel: AuthViewModel,
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
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(name = R.drawable.ic_arrow_back)
            }
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
            contentPadding = PaddingValues(bottom = 100.dp) // leave space for button
        ) {
            items(
                count = allTickets.size,
                key = { index -> allTickets[index]._id }
            ) { index ->
                val ticket = allTickets[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(ticket.name, style = MaterialTheme.typography.bodyLarge)
                    }
                    TextButton(onClick = { ticketsViewModel.deleteTicket(ticket._id) }) {
                        Text("Remove")
                    }
                }
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
