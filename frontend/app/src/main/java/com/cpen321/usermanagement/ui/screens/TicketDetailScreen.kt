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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import com.cpen321.usermanagement.data.remote.dto.TicketsUiState
import com.cpen321.usermanagement.ui.viewmodels.AuthViewModel
import com.cpen321.usermanagement.ui.viewmodels.TicketsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticket: BingoTicket,
    onBackClick: () -> Unit,
    viewModel: TicketsViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ticket.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(name = R.drawable.ic_arrow_back)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BingoGridInteractive(ticket = ticket, viewModel = viewModel)
        }
    }
}

@Composable
fun BingoGridInteractive(ticket: BingoTicket, viewModel: TicketsViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (row in 0 until 3) {
            Row(horizontalArrangement = Arrangement.Center) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    val isCrossed = ticket.crossedOff?.getOrNull(index) ?: false
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(100.dp)
                            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .background(if (isCrossed) Color.Green.copy(alpha = 0.4f) else Color.White)
                            .clickable {
                                viewModel.toggleSquare(ticket._id, index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ticket.events.getOrNull(index) ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}