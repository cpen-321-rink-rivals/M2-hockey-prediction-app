package com.cpen321.usermanagement.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun ChallengeDetailsScreen(
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
            Log.d("ChallengeDetailsScreen", "Challenge updated: ${event.message}")
            if (event.challengeId == challengeId) {
                challengesViewModel.loadChallenge(challengeId)
            }
        }
    }

    LaunchedEffect(uiState.user) {
        challengesViewModel.loadFriends(uiState.user!!._id)
        challengesViewModel.loadAvailableTickets(uiState.user!!._id)
        challengesViewModel.loadUpcomingGames()
    }

    // Load all members' tickets
    LaunchedEffect(uiState.selectedChallenge) {
        if (uiState.selectedChallenge != null) {
            // Load tickets for all members who have ticket IDs
            uiState.selectedChallenge?.ticketIds.orEmpty().forEach { (userId, ticketId) ->
                challengesViewModel.loadTicketById(ticketId)
            }
        }
    }

    // State variables
    var selectedTicketForJoining by remember { mutableStateOf<BingoTicket?>(null) }
    val availableTicketsForJoining = uiState.availableTicketsForJoining
    val challenge = uiState.selectedChallenge
    val userId = uiState.user?._id
    val allFriends = uiState.allFriends
    val upcomingGames = uiState.availableGames // Get games list
    val allTickets = uiState.challengeTickets // This now includes all loaded tickets

    if (challenge != null && challenge.id == challengeId) {
        val isOwner = challenge.ownerId == userId
        val isInvitee = challenge.invitedUserIds.contains(userId)
        val canLeave = !isOwner && challenge.memberIds.contains(userId)

        ChallengeDetailsContent(
            challenge = challenge,
            user = uiState.user,
            isOwner = isOwner,
            isInvitee = isInvitee,
            canLeave = canLeave,
            onBackClick = onBackClick,
            onSaveChallenge = { updatedChallenge ->
                challengesViewModel.updateChallenge(updatedChallenge)
                onBackClick()
            },
            onDeleteChallenge = {
                challengesViewModel.deleteChallenge(challenge.id)
                onBackClick()
            },
            allFriends = allFriends,
            availableTickets = availableTicketsForJoining.orEmpty().filter { 
                it.game.id.toString() == challenge.gameId 
            },
            selectedTicket = selectedTicketForJoining,
            onTicketSelected = { ticket -> selectedTicketForJoining = ticket },
            onJoinChallenge = {
                Log.d("ChallengeDetailsScreen", "Joining challenge with ticket: $selectedTicketForJoining")
                selectedTicketForJoining?.let { ticket ->
                    challengesViewModel.joinChallenge(challenge.id, ticketId = ticket._id)
                    onBackClick()
                }
            },
            onLeaveChallenge = {
                challengesViewModel.leaveChallenge(challenge.id)
                onBackClick()
            },
            onDeclineInvitation = {
                challengesViewModel.declineInvitation(challenge.id)
                onBackClick()
            },
            upcomingGames = upcomingGames,
            allTickets = allTickets
        )
    } else {
        Log.e("ChallengeDetailsScreen", "Challenge not found")
        return
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChallengeDetailsContent(
    challenge: Challenge,
    user: User?,
    isOwner: Boolean,
    isInvitee: Boolean,
    canLeave: Boolean,
    onBackClick: () -> Unit,
    onSaveChallenge: (Challenge) -> Unit,
    onDeleteChallenge: () -> Unit,
    allFriends: List<Friend>?,
    availableTickets: List<BingoTicket>,
    selectedTicket: BingoTicket?,
    onTicketSelected: (BingoTicket) -> Unit,
    onJoinChallenge: () -> Unit,
    onLeaveChallenge: () -> Unit,
    onDeclineInvitation: () -> Unit,
    upcomingGames: List<com.cpen321.usermanagement.data.remote.dto.Game>?,
    allTickets: List<BingoTicket>?
) {
    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(challenge.title) }
    var description by remember { mutableStateOf(challenge.description) }
    var maxMembers by remember { mutableStateOf(challenge.maxMembers.toString()) }

    val canEdit = isOwner && (challenge.status == ChallengeStatus.PENDING || challenge.status == ChallengeStatus.ACTIVE)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Challenge Details",
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
                        if (isEditing) {
                            TextButton(
                                onClick = {
                                    isEditing = false
                                    // Reset values
                                    title = challenge.title
                                    description = challenge.description
                                    maxMembers = challenge.maxMembers.toString()
                                }
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    val updatedChallenge = challenge.copy(
                                        title = title,
                                        description = description,
                                        maxMembers = maxMembers.toIntOrNull() ?: challenge.maxMembers
                                    )
                                    onSaveChallenge(updatedChallenge)
                                    isEditing = false
                                }
                            ) {
                                Text("Save")
                            }
                        } else {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = if (isOwner || canLeave || isInvitee) 80.dp else 16.dp)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Challenge Title - Big and Prominent
                ChallengeTitleSection(
                    title = if (isEditing) title else challenge.title,
                    description = if (isEditing) description else challenge.description,
                    status = challenge.status,
                    isEditing = isEditing,
                    onTitleChange = { title = it },
                    onDescriptionChange = { description = it }
                )

                // Game Information Card
                GameInfoCard(
                    challenge = challenge,
                    upcomingGames = upcomingGames
                )

                // Members Section - Clear and Organized
                MembersSection(
                    challenge = challenge,
                    allFriends = allFriends,
                    user = user,
                    isEditing = isEditing,
                    maxMembers = maxMembers,
                    onMaxMembersChange = { maxMembers = it },
                    allTickets = allTickets
                )

                // Invited Users Section - Separate and Clear
                if (challenge.invitedUserIds.isNotEmpty()) {
                    InvitedUsersSection(
                        challenge = challenge,
                        allFriends = allFriends
                    )
                }
            }

            // Bottom action buttons
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
                    onDeclineClick = onDeclineInvitation,
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
private fun ChallengeTitleSection(
    title: String,
    description: String,
    status: ChallengeStatus,
    isEditing: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status Badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = when (status) {
                    ChallengeStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                    ChallengeStatus.ACTIVE -> MaterialTheme.colorScheme.tertiaryContainer
                    ChallengeStatus.LIVE -> MaterialTheme.colorScheme.errorContainer
                    ChallengeStatus.FINISHED -> MaterialTheme.colorScheme.surfaceVariant
                    ChallengeStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = status.name,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (status) {
                        ChallengeStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                        ChallengeStatus.ACTIVE -> MaterialTheme.colorScheme.onTertiaryContainer
                        ChallengeStatus.LIVE -> MaterialTheme.colorScheme.onErrorContainer
                        ChallengeStatus.FINISHED -> MaterialTheme.colorScheme.onSurfaceVariant
                        ChallengeStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Title
            if (isEditing) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Challenge Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Description
            if (isEditing) {
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            } else {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun GameInfoCard(
    challenge: Challenge,
    upcomingGames: List<com.cpen321.usermanagement.data.remote.dto.Game>?
) {
    // Find the game that matches the challenge's gameId
    val game = upcomingGames?.firstOrNull { it.id.toString() == challenge.gameId }

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
                    painter = painterResource(id = R.drawable.hockey_outlined_icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Game Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider()

            if (game != null) {
                // Show game matchup
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = game.awayTeam.abbrev,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = " @ ",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = game.homeTeam.abbrev,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Show game time
                Text(
                    text = formatGameTime(game.startTimeUTC),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Show venue
                Text(
                    text = game.venue.default,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Fallback if game data not loaded
                InfoRow(
                    icon = R.drawable.hockey_outlined_icon,
                    label = "Game ID",
                    value = challenge.gameId
                )
            }
        }
    }
}

// Helper function to format game time
private fun formatGameTime(startTimeUTC: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(startTimeUTC)
        
        val outputFormat = SimpleDateFormat("EEE, MMM dd 'at' h:mm a", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: startTimeUTC
    } catch (e: Exception) {
        startTimeUTC
    }
}

@Composable
private fun MembersSection(
    challenge: Challenge,
    allFriends: List<Friend>?,
    user: User?,
    isEditing: Boolean,
    maxMembers: String,
    onMaxMembersChange: (String) -> Unit,
    allTickets: List<BingoTicket>?
) {
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.group_outlined_icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Members",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Member count badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${challenge.memberIds.size}/${challenge.maxMembers}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            if (isEditing) {
                OutlinedTextField(
                    value = maxMembers,
                    onValueChange = onMaxMembersChange,
                    label = { Text("Max Members") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Divider()

            if (challenge.memberIds.isNotEmpty() && allFriends != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    challenge.memberIds.forEach { memberId ->
                        val memberName = when (memberId) {
                            challenge.ownerId -> "${allFriends.find { it.id == memberId }?.name ?: user?.name ?: memberId}"
                            user?._id -> "${user.name}"
                            else -> allFriends.find { it.id == memberId }?.name ?: memberId
                        }

                        // Get the ticket ID for this member
                        val ticketId = challenge.ticketIds[memberId]
                        // Try to find the ticket in our loaded tickets
                        val memberTicket = allTickets?.firstOrNull { it._id == ticketId }

                        MemberRowWithTicket(
                            name = memberName,
                            isOwner = memberId == challenge.ownerId,
                            isYou = memberId == user?._id,
                            ticket = memberTicket,
                            hasTicketId = ticketId != null
                        )
                    }
                }
            } else {
                Text(
                    text = "No members yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun InvitedUsersSection(
    challenge: Challenge,
    allFriends: List<Friend>?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.calendar_clock_icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Pending Invitations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    Text(
                        text = "${challenge.invitedUserIds.size}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            Divider()

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                challenge.invitedUserIds.forEach { invitedId ->
                    val invitedName = allFriends?.find { it.id == invitedId }?.name ?: invitedId

                    InvitedUserRow(name = invitedName)
                }
            }
        }
    }
}

@Composable
private fun MemberRow(
    name: String,
    isOwner: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isOwner) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isOwner) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isOwner) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun MemberRowWithTicket(
    name: String,
    isOwner: Boolean,
    isYou: Boolean,
    ticket: BingoTicket?,
    hasTicketId: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isYou) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Member info header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isOwner) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isOwner) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (isOwner) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "Owner",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        if (isYou) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.secondary
                            ) {
                                Text(
                                    text = "You",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                    }
                    
                    if (ticket != null) {
                        Text(
                            text = ticket.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (hasTicketId) {
                        Text(
                            text = "Ticket selected ✓",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    } else {
                        Text(
                            text = "No ticket selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Show bingo ticket grid if available
            if (ticket != null) {
                Divider()
                BingoGridPreview(events = ticket.events)
            }
        }
    }
}

@Composable
private fun InvitedUserRow(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.calendar_clock_icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
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

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Challenge") },
            text = { Text("Are you sure you want to permanently delete this challenge? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onClick()
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

    Button(
        onClick = { showDialog = true },
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
    onTicketSelected: (BingoTicket) -> Unit,
    onJoinClick: () -> Unit,
    onDeclineClick: () -> Unit,
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
                        .menuAnchor()
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDeclineClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Decline")
                }

                Button(
                    onClick = onJoinClick,
                    enabled = selectedTicket != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.group_outlined_icon),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept")
                }
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

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Leave Challenge") },
            text = { Text("Are you sure you want to leave this challenge?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onClick()
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

    Button(
        onClick = { showDialog = true },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Leave Challenge")
    }
}
