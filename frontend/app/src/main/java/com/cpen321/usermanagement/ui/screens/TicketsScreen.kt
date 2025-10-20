package com.cpen321.usermanagement.ui.screens

import Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.cpen321.usermanagement.ui.viewmodels.AuthViewModel
import com.cpen321.usermanagement.ui.viewmodels.ChallengesUiState
import com.cpen321.usermanagement.ui.viewmodels.TicketsUiState
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

    // Side effects
    LaunchedEffect(Unit) {
        ticketsViewModel.clearSuccessMessage()
        ticketsViewModel.clearError()
    }

    TicketsContent(
        uiState = uiState,
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
    callbacks: TicketsScreenCallbacks
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TicketsTopBar(
                onBackClick = { callbacks.onBackClick() }
            )
        }
    ) { paddingValues ->
        TicketsBody(
            paddingValues = paddingValues,
            isLoading = uiState.isLoadingTickets,
            allTickets = uiState.allTickets,
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
    allTickets: List<String>,
    isLoading: Boolean,
    onCreateTicketClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when {
            isLoading -> {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                TicketsList(
                    modifier = Modifier.fillMaxSize(),
                    allTickets = allTickets
                )
                AddTicketButton(
                    onClick = onCreateTicketClick,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp).width(200.dp).height(60.dp)

                )
            }
        }
    }
}

@Composable
fun TicketsList(
    modifier: Modifier = Modifier,
    allTickets: List<String> = emptyList(),
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 10.dp)
    ) {
        LazyColumn {
            items(
                count = allTickets.size,
                key = { index -> allTickets[index] }
            ) {
                ListItem(
                    headlineContent = { Text(allTickets[it]) },
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(10.dp)
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }
    }
}
@Composable
private fun AddTicketButton(
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Button(
        onClick = { onClick() },
        modifier = modifier,
        ) {
        Text("New Bingo Ticket")
    }

}

@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(modifier = modifier)
}