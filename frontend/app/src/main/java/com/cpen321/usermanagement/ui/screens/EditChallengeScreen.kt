package com.cpen321.usermanagement.ui.screens

// Your existing imports...
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.local.preferences.SocketEventListener
import com.cpen321.usermanagement.data.local.preferences.SocketManager
import com.cpen321.usermanagement.data.remote.dto.BingoTicket
import com.cpen321.usermanagement.data.remote.dto.Challenge
import com.cpen321.usermanagement.data.remote.dto.ChallengeStatus
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.ui.viewmodels.ChallengesViewModel
import com.cpen321.usermanagement.ui.viewmodels.Friend
import java.text.SimpleDateFormat
import java.util.*




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChallengeScreen(
    challengeId: String,
    challengesViewModel: ChallengesViewModel,
    socketManager: SocketManager,
    socketEventListener: SocketEventListener,
    onBackClick: () -> Unit
) {
    val uiState by challengesViewModel.uiState.collectAsState()

    // Side effects

    LaunchedEffect(challengeId) {
        challengesViewModel.loadChallenge(challengeId)
        socketManager.joinChallengeRoom(challengeId)
        challengesViewModel.loadProfile()
    }
    
    // Listen for challenge updates via WebSocket
    LaunchedEffect(challengeId) {
        socketEventListener.challengeUpdated.collect { event ->
            Log.d("EditChallengeScreen", "Challenge updated: ${event.message}")
            // Check if this update is for our challenge
            if (event.challengeId == challengeId) {
                // Reload the challenge to get fresh data
                challengesViewModel.loadChallenge(challengeId)
            }
        }
    }
    LaunchedEffect(uiState.user) {
        challengesViewModel.loadFriends(uiState.user!!._id)
        challengesViewModel.loadAvailableTickets(uiState.user!!._id)
        challengesViewModel.loadUpcomingGames()
    }

    // State variables // BingoTicket data class should be changed.
    var selectedTicketForJoining by remember { mutableStateOf<BingoTicket?>(null) }
    val availableTicketsForJoining = uiState.availableTickets

    // These states now live here, to be controlled by the screen
    val challenge = uiState.selectedChallenge
    val userId = uiState.user?._id
    val allFriends = uiState.allFriends



    // make sure the challenge is not null and that it's the correct challenge (avoid stale data)
    if (challenge != null && challenge.id == challengeId) {
        val isOwner = challenge.ownerId == userId
        val isInvitee = challenge.invitedUserIds.contains(userId)
        val canLeave = !isOwner && challenge.memberIds.contains(userId)

        EditChallengeContent(
            challenge = challenge,
            user = uiState.user,
            isOwner,
            isInvitee,
            canLeave,
            onBackClick = onBackClick,
            onSaveChallenge = { updatedChallenge ->
                challengesViewModel.updateChallenge(updatedChallenge)
                onBackClick() // Navigate back after saving
            },
            onDeleteChallenge = {
                challengesViewModel.deleteChallenge(challenge.id)
                onBackClick() // Navigate back after deleting
            },
            allFriends = allFriends,
            availableTickets = availableTicketsForJoining.orEmpty().filter { it.game.id.toString() == challenge.gameId }, // Filter tickets for the correct game
            selectedTicket = selectedTicketForJoining,
            onTicketSelected = { ticket -> selectedTicketForJoining = ticket },
            onJoinChallenge = {
                Log.d("EditChallengeScreen", "Joining challenge with ticket: $selectedTicketForJoining")
                selectedTicketForJoining?.let { ticket ->
                    challengesViewModel.joinChallenge(challenge.id, ticketId = ticket._id)
                    onBackClick() // Navigate back after joining
                }
            },
            onLeaveChallenge = {
                challengesViewModel.leaveChallenge(challenge.id)
                onBackClick() // Navigate back after leaving
            }
        )
    } else {
        Log.e("EditChallengeScreen", "Challenge not found")
        return
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditChallengeContent(
    challenge: Challenge,
    user: User?,
    isOwner: Boolean,
    isInvitee: Boolean,
    canLeave: Boolean,
    onBackClick: () -> Unit,
    onSaveChallenge: (Challenge) -> Unit,
    onDeleteChallenge: () -> Unit, // Added this callback
    allFriends: List<Friend>?,
    availableTickets: List<BingoTicket>,
    selectedTicket: BingoTicket?,
    onTicketSelected: (BingoTicket) -> Unit,
    onJoinChallenge: () -> Unit,
    onLeaveChallenge: () -> Unit
) {
    var title by remember { mutableStateOf(challenge.title) }
    var description by remember { mutableStateOf(challenge.description) }
    var maxMembers by remember { mutableStateOf(challenge.maxMembers.toString()) }

    // A challenge can be edited by the owner if it's still pending or active.
    val canEdit = isOwner && (challenge.status == ChallengeStatus.PENDING || challenge.status == ChallengeStatus.ACTIVE)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Challenge",
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
                    if (canEdit) {
                        Button(
                            onClick = {
                                val updatedChallenge = challenge.copy(
                                    title = title,
                                    description = description,
                                    maxMembers = maxMembers.toIntOrNull() ?: challenge.maxMembers
                                )
                                onSaveChallenge(updatedChallenge)
                            }
                        ) {
                            Text("Save")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        // Use a Box to allow placing the delete button at the bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    // Add padding at the bottom to make space for the delete button
                    .padding(bottom = 80.dp)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusCard(challenge = challenge)

                if (canEdit) {
                    EditFieldsCard(
                        title = title,
                        onTitleChange = { title = it },
                        description = description,
                        onDescriptionChange = { description = it },
                        maxMembers = maxMembers,
                        onMaxMembersChange = { maxMembers = it }
                    )
                } else {
                    ReadOnlyFieldsCard(challenge = challenge)
                }

                GameInfoCard(challenge = challenge)
                MembersCard(challenge = challenge, allFriends = allFriends, user = user)
            }

            // Delete button at the bottom
            if (isOwner) {
                DeleteChallengeButton(
                    onClick = onDeleteChallenge,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
            if (canLeave) {
                LeaveChallengeButton(
                    onClick = onLeaveChallenge,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
            if (isInvitee) {
                JoinChallengeCard(
                    availableTickets = availableTickets,
                    selectedTicket = selectedTicket,
                    onTicketSelected = onTicketSelected,
                    onJoinClick = onJoinChallenge,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatusCard(challenge: Challenge) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Column {
                Text(
                    text = "Challenge Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = challenge.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EditFieldsCard(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    maxMembers: String,
    onMaxMembersChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Edit Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

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
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            OutlinedTextField(
                value = maxMembers,
                onValueChange = onMaxMembersChange,
                label = { Text("Max Members") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun ReadOnlyFieldsCard(challenge: Challenge) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoRow(
                icon = R.drawable.ic_edit,
                label = "Title",
                value = challenge.title
            )

            InfoRow(
                icon = R.drawable.info_outlined_icon,
                label = "Description",
                value = challenge.description
            )

            InfoRow(
                icon = R.drawable.group_outlined_icon,
                label = "Max Members",
                value = challenge.maxMembers.toString()
            )
        }
    }
}

@Composable
private fun GameInfoCard(challenge: Challenge) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.hockey_outlined_icon), contentDescription = null)
                Text(
                    text = "Game Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            InfoRow(
                icon = R.drawable.hockey_outlined_icon,
                label = "Game ID",
                value = challenge.gameId
            )

            /*challenge.gameStartTime?.let { startTime ->
                InfoRow(
                    icon = R.drawable.calendar_clock_icon,
                    label = "Game Start Time",
                    value = formatDateTime(startTime)
                )
            }*/ // TODO: fix problem with date type
        }
    }
}

@Composable
private fun MembersCard(challenge: Challenge, allFriends: List<Friend>?, user: User?) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.group_outlined_icon),
                    contentDescription = null
                )
                Text(
                    text = "Members (${challenge.memberIds.size}/${challenge.maxMembers})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (challenge.memberIds.isNotEmpty() && allFriends != null) {
                challenge.memberIds.forEach { memberId ->

                    // match memberId to user or friends list
                    val memberName = when (memberId) {
                        challenge.ownerId -> "${allFriends.find { it.id == memberId }?.name ?: user?.name ?: memberId} (Owner)"
                        user?._id -> "${user.name} (You)"
                        else -> allFriends.find { it.id == memberId }?.name ?: memberId
                    }



                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.group_outlined_icon),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = memberName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Text(
                    text = "No members yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (challenge.invitedUserIds.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Pending Invitations (${challenge.invitedUserIds.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                challenge.invitedUserIds.forEach { invitedId ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar_clock_icon),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = invitedId,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DeleteChallengeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    // The confirmation dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Challenge") },
            text = { Text("Are you sure you want to permanently delete this challenge? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onClick() // This calls viewModel.deleteChallenge(...)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // The actual button shown on the screen
    Button(
        onClick = { showDialog = true }, // Show the dialog on click
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Delete Challenge")
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinChallengeCard(
    availableTickets: List<BingoTicket>,
    selectedTicket: BingoTicket?,
    onTicketSelected: (BingoTicket) -> Unit,    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "You're Invited!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Exposed Dropdown Menu for ticket selection
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedTicket?.name ?: "Select a ticket to use",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Your Bingo Ticket") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier
                        .menuAnchor() // Anchor for the dropdown
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    if (availableTickets.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No tickets available for this game") },
                            onClick = { },
                            enabled = false
                        )
                    } else {
                        availableTickets.forEach { ticket ->
                            DropdownMenuItem(
                                text = { Text(ticket.name) },
                                onClick = {
                                    onTicketSelected(ticket)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onJoinClick,
                enabled = selectedTicket != null, // Button is enabled only when a ticket is selected
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.group_outlined_icon),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Accept Invitation & Join")
            }
        }
    }
}

@Composable
private fun LeaveChallengeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    // The confirmation dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Leave Challenge") },
            text = { Text("Are you sure you want to leave this challenge?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onClick() // This calls viewModel.deleteChallenge(...)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // The actual button shown on the screen
    Button(
        onClick = { showDialog = true }, // Show the dialog on click
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Leave Challenge")
    }
}

