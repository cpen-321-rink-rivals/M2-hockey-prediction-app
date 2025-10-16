package com.cpen321.usermanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.remote.dto.CreateChallengeRequest
import com.cpen321.usermanagement.ui.viewmodels.ChallengesViewModel
import java.text.SimpleDateFormat
import java.util.*

// Dummy data models - replace with your actual data models
data class Game(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val startTime: String,
    val title: String = "$homeTeam vs $awayTeam"
)

data class BingoTicket(
    val id: String,
    val gameId: String,
    val title: String,
    val events: List<String> = emptyList()
)

data class Friend(
    val id: String,
    val username: String,
    val displayName: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeScreen(
    challengesViewModel: ChallengesViewModel,
    onBackClick: () -> Unit,
) {
    // State variables
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var selectedTicket by remember { mutableStateOf<BingoTicket?>(null) }
    var challengeTitle by remember { mutableStateOf("") }
    var challengeDescription by remember { mutableStateOf("") }
    var maxMembers by remember { mutableStateOf("10") }
    var selectedFriends by remember { mutableStateOf<Set<Friend>>(emptySet()) }
    
    // Dialog states
    var showGamePicker by remember { mutableStateOf(false) }
    var showTicketPicker by remember { mutableStateOf(false) }
    var showFriendsPicker by remember { mutableStateOf(false) }
    
    // Dummy data - replace with actual data from ViewModel
    val availableGames = remember {
        listOf(
            Game("game1", "Rangers", "Devils", "2024-01-20T19:00:00Z"),
            Game("game2", "Bruins", "Leafs", "2024-01-21T20:00:00Z"),
            Game("game3", "Penguins", "Flyers", "2024-01-22T19:30:00Z")
        )
    }
    
    val availableTickets = remember {
        listOf(
            BingoTicket("ticket1", "game1", "Rangers vs Devils Predictions"),
            BingoTicket("ticket2", "game1", "My Rangers Ticket"),
            BingoTicket("ticket3", "game2", "Bruins Game Ticket"),
            BingoTicket("ticket4", "game3", "Penguins vs Flyers")
        )
    }
    
    val availableFriends = remember {
        listOf(
            Friend("friend1", "john_doe", "John Doe"),
            Friend("friend2", "sarah_123", "Sarah Wilson"),
            Friend("friend3", "mike_hockey", "Mike Chen"),
            Friend("friend4", "emma_fan", "Emma Thompson")
        )
    }
    
    // Filter tickets based on selected game
    val gameTickets = availableTickets.filter { it.gameId == selectedGame?.id }
    
    // Update challenge title when game changes
    LaunchedEffect(selectedGame) {
        if (selectedGame != null && challengeTitle.isEmpty()) {
            challengeTitle = selectedGame!!.title
        }
    }
    
    // Validation
    val canCreateChallenge = selectedGame != null && 
                           selectedTicket != null && 
                           challengeTitle.isNotBlank() &&
                           maxMembers.toIntOrNull() != null &&
                           maxMembers.toInt() >= 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Create Challenge",
                    fontWeight = FontWeight.Bold
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        if (canCreateChallenge) {
                            // Create challenge request
                            val challengeRequest = CreateChallengeRequest(
                                title = challengeTitle,
                                description = challengeDescription.ifBlank { "Join my hockey prediction challenge!" },
                                gameId = selectedGame!!.id,
                                invitedUserIds = selectedFriends.map { it.id },
                                maxMembers = maxMembers.toIntOrNull(),
                                //gameStartTime = selectedGame!!.startTime.
                            )
                            
                            challengesViewModel.createChallenge(challengeRequest)
                            onBackClick() // navigate back to challenges screen
                            

                        }
                    },
                    enabled = canCreateChallenge
                ) {
                    Text("Create")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Game Selection Card
            SelectionCard(
                title = "Select Game",
                icon = Icons.Default.Build,
                selectedText = selectedGame?.title ?: "Choose a game",
                onClick = { showGamePicker = true },
                isSelected = selectedGame != null
            )
            
            // Ticket Selection Card  
            SelectionCard(
                title = "Select Bingo Ticket",
                icon = Icons.Default.Check,
                selectedText = selectedTicket?.title ?: if (selectedGame != null) "Choose your ticket" else "Select a game first",
                onClick = { if (selectedGame != null) showTicketPicker = true },
                isSelected = selectedTicket != null,
                enabled = selectedGame != null
            )
            
            // Challenge Details Card
            ChallengeDetailsCard(
                title = challengeTitle,
                onTitleChange = { challengeTitle = it },
                description = challengeDescription,
                onDescriptionChange = { challengeDescription = it },
                maxMembers = maxMembers,
                onMaxMembersChange = { maxMembers = it }
            )
            
            // Friends Selection Card
            FriendsSelectionCard(
                selectedFriends = selectedFriends,
                onClick = { showFriendsPicker = true }
            )
            
            // Create Button (Alternative to top bar)
            if (!canCreateChallenge) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Please complete all required fields:\n• Select a game\n• Select a bingo ticket\n• Enter challenge title\n• Set max members (≥2)",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
    
    // Dialogs
    if (showGamePicker) {
        GamePickerDialog(
            games = availableGames,
            selectedGame = selectedGame,
            onGameSelected = { game ->
                selectedGame = game
                selectedTicket = null // Reset ticket when game changes
                showGamePicker = false
            },
            onDismiss = { showGamePicker = false }
        )
    }
    
    if (showTicketPicker) {
        TicketPickerDialog(
            tickets = gameTickets,
            selectedTicket = selectedTicket,
            onTicketSelected = { ticket ->
                selectedTicket = ticket
                showTicketPicker = false
            },
            onDismiss = { showTicketPicker = false }
        )
    }
    
    if (showFriendsPicker) {
        FriendsPickerDialog(
            friends = availableFriends,
            selectedFriends = selectedFriends,
            onFriendsSelected = { friends ->
                selectedFriends = friends
                showFriendsPicker = false
            },
            onDismiss = { showFriendsPicker = false }
        )
    }
}

@Composable
private fun SelectionCard(
    title: String,
    icon: ImageVector,
    selectedText: String,
    onClick: () -> Unit,
    isSelected: Boolean,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = selectedText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.ArrowForward,
                contentDescription = null,
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChallengeDetailsCard(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    maxMembers: String,
    onMaxMembersChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Challenge Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Challenge Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                placeholder = { Text("Add a description for your challenge...") }
            )

            OutlinedTextField(
                value = maxMembers,
                onValueChange = onMaxMembersChange,
                label = { Text("Max Members") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("Minimum 2 members") }
            )
        }
    }
}

@Composable
private fun FriendsSelectionCard(
    selectedFriends: Set<Friend>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Invite Friends",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (selectedFriends.isEmpty()) 
                        "No friends selected" 
                    else 
                        "${selectedFriends.size} friend${if (selectedFriends.size != 1) "s" else ""} selected",
                    style = MaterialTheme.typography.bodyLarge
                )
                if (selectedFriends.isNotEmpty()) {
                    Text(
                        text = selectedFriends.take(3).joinToString(", ") { it.displayName ?: it.username } +
                               if (selectedFriends.size > 3) " and ${selectedFriends.size - 3} more" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GamePickerDialog(
    games: List<Game>,
    selectedGame: Game?,
    onGameSelected: (Game) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Game",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(games) { game ->
                        GameItem(
                            game = game,
                            isSelected = game.id == selectedGame?.id,
                            onClick = { onGameSelected(game) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameItem(
    game: Game,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = game.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatDateTime(game.startTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TicketPickerDialog(
    tickets: List<BingoTicket>,
    selectedTicket: BingoTicket?,
    onTicketSelected: (BingoTicket) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Bingo Ticket",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (tickets.isEmpty()) {
                    Text(
                        text = "No tickets available for this game",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tickets) { ticket ->
                            TicketItem(
                                ticket = ticket,
                                isSelected = ticket.id == selectedTicket?.id,
                                onClick = { onTicketSelected(ticket) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketItem(
    ticket: BingoTicket,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = ticket.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun FriendsPickerDialog(
    friends: List<Friend>,
    selectedFriends: Set<Friend>,
    onFriendsSelected: (Set<Friend>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelectedFriends by remember { mutableStateOf(selectedFriends) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Friends",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { 
                            onFriendsSelected(tempSelectedFriends)
                            onDismiss()
                        }
                    ) {
                        Text("Done")
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(friends) { friend ->
                        FriendItem(
                            friend = friend,
                            isSelected = friend in tempSelectedFriends,
                            onToggle = { 
                                tempSelectedFriends = if (friend in tempSelectedFriends) {
                                    tempSelectedFriends - friend
                                } else {
                                    tempSelectedFriends + friend
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendItem(
    friend: Friend,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.displayName ?: friend.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (friend.displayName != null) {
                    Text(
                        text = "@${friend.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun formatDateTime(dateTimeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateTimeString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateTimeString // Return original string if parsing fails
    }
}

