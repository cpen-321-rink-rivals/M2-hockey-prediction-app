package com.cpen321.usermanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import com.cpen321.usermanagement.ui.viewmodels.TicketsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticket: BingoTicket,
    onBackClick: () -> Unit,
    viewModel: TicketsViewModel,
) {
    LaunchedEffect(ticket._id) {
        viewModel.updateTicketFromBoxscore(ticket._id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ticket.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(id = R.string.back)
                        )
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
    val nhlDataManager = viewModel.nhlDataManager

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (row in 0 until 3) {
            Row(horizontalArrangement = Arrangement.Center) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    val isCrossed = ticket.crossedOff.getOrNull(index) ?: false
                    val event = ticket.events.getOrNull(index)

                    // Remember formatted label
                    val label = remember(event) { mutableStateOf("") }
                    LaunchedEffect(event) {
                        if (event != null) {
                            label.value = nhlDataManager.formatEventLabel(event)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(100.dp)
                            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .background(if (isCrossed) Color.Green.copy(alpha = 0.4f) else Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label.value,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
