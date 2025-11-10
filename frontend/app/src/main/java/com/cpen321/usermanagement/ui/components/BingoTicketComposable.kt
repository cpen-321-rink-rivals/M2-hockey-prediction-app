package com.cpen321.usermanagement.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cpen321.usermanagement.data.local.preferences.EventCondition
import com.cpen321.usermanagement.data.local.preferences.NhlDataManager
import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import com.cpen321.usermanagement.ui.components.TeamLogo

data class BingoScore(
    val noCrossedOff: Int = 0,
    val noRows: Int = 0,
    val noColumns: Int = 0,
    val noCrosses: Int = 0,
    val total: Int = 0
)

/** Compute score client-side (mirror backend logic) */
fun computeBingoScore(crossedOff: List<Boolean>): BingoScore {
    val arr = crossedOff.take(9) + List(maxOf(0, 9 - crossedOff.size)) { false }
    val noCrossedOff = arr.count { it }

    var noRows = 0
    var noColumns = 0
    var noCrosses = 0

    for (r in 0 until 3) {
        val start = r * 3
        if (arr[start] && arr[start+1] && arr[start+2]) noRows++
    }
    for (c in 0 until 3) {
        if (arr[c] && arr[c+3] && arr[c+6]) noColumns++
    }
    if (arr[0] && arr[4] && arr[8]) noCrosses++
    if (arr[2] && arr[4] && arr[6]) noCrosses++

    val perSquare = noCrossedOff * 1
    val perLine = (noRows + noColumns + noCrosses) * 3
    val bingoBonus = if (noCrossedOff == 9) 10 else 0
    val total = perSquare + perLine + bingoBonus

    return BingoScore(noCrossedOff, noRows, noColumns, noCrosses, total)
}

@Composable
fun BingoGrid(
    events: List<EventCondition>,
    crossedOff: List<Boolean>,
    nhlDataManager: NhlDataManager,
    cellSize: Dp = 80.dp,
    onToggle: ((index: Int, newValue: Boolean) -> Unit)? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (row in 0 until 3) {
            Row(horizontalArrangement = Arrangement.Center) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    val isCrossed = crossedOff.getOrNull(index) ?: false
                    val event = events.getOrNull(index)

                    val label = remember(event) { mutableStateOf("") }
                    LaunchedEffect(event) {
                        if (event != null) label.value = nhlDataManager.formatEventLabel(event)
                    }

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
                            .size(cellSize)
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .background(
                                if (isCrossed) Color.Green.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .let { m -> if (onToggle != null) m.clickable { onToggle(index, !isCrossed) } else m },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!logoUrl.isNullOrBlank()) {
                            TeamLogo(
                                logoUrl = logoUrl,
                                teamAbbrev = label.value,
                                size = (cellSize.value / 2).dp,
                                showAbbrev = true,
                                abbrevFontSize = 9.sp
                            )
                        } else {
                            Text(text = label.value, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BingoTicketCard(
    ticket: BingoTicket,
    nhlDataManager: NhlDataManager,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val score = ticket.score?.let { BingoScore(it.noCrossedOff, it.noRows, it.noColumns, it.noCrosses, it.total) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(ticket.name, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            BingoGrid(events = ticket.events, crossedOff = ticket.crossedOff, nhlDataManager = nhlDataManager)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Score: ${score?.total ?: "N/A"}")
            if (onDelete != null) {
                TextButton(onClick = { onDelete() }) { Text("Delete") }
            }
        }
    }
}

@Composable
fun BingoTicketDetailInteractive(
    ticket: BingoTicket,
    nhlDataManager: NhlDataManager,
    onApply: ((List<Boolean>) -> Unit)? = null
) {
    var crossed by remember { mutableStateOf(ticket.crossedOff.toMutableList()) }
    val score = computeBingoScore(crossed)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Text(ticket.name, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        BingoGrid(events = ticket.events, crossedOff = crossed, nhlDataManager = nhlDataManager, cellSize = 100.dp) { idx, newValue ->
            crossed = crossed.toMutableList().also { it[idx] = newValue }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Breakdown:")
        Text("Fields crossed: ${score.noCrossedOff}")
        Text("Rows: ${score.noRows} (x3 each)\nColumns: ${score.noColumns} (x3 each)\nDiagonals: ${score.noCrosses} (x3 each)")
        Text("Bonus (bingo): ${if (score.noCrossedOff == 9) 10 else 0}")
        Text("Total: ${score.total}", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onApply?.invoke(crossed) }) { Text("Apply") }
            Button(onClick = { crossed = ticket.crossedOff.toMutableList() }) { Text("Reset") }
        }
    }
}
