package com.cpen321.usermanagement.ui.screens

import Icon
import android.graphics.drawable.Icon
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.cpen321.usermanagement.ui.viewmodels.ChallengesViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toLowerCase
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.data.local.preferences.SocketEventListener
import com.cpen321.usermanagement.data.remote.dto.Challenge
import com.cpen321.usermanagement.ui.viewmodels.ChallengesUiState


private data class ChallengesScreenCallbacks(
    val onBackClick: () -> Unit,
    val onAddChallengeClick: () -> Unit,
    val onChallengeClick: (challengeId: String) -> Unit
)

data class ChallengesScreenActions(
    val onBackClick: () -> Unit,
    val onAddChallengeClick: () -> Unit,
    val onChallengeClick: (challengeId: String) -> Unit
)

private const val TAG = "ChallengesScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    challengesViewModel: ChallengesViewModel,
    socketEventListener: SocketEventListener,
    actions: ChallengesScreenActions

) {

    val uiState by challengesViewModel.uiState.collectAsState()

    // Side effects
    LaunchedEffect(Unit) {
        challengesViewModel.clearSuccessMessage()
        challengesViewModel.clearError()
        challengesViewModel.loadProfile()
        challengesViewModel.loadChallenges()
    }
    
    // Listen for challenge invitations
    LaunchedEffect(Unit) {
        socketEventListener.challengeInvitations.collect { event ->
            Log.d(TAG, "Challenge invitation received: ${event.message}")
            // Reload challenges to show the new invitation
            challengesViewModel.loadChallenges()
        }
    }
    
    // Listen for challenge updates
    LaunchedEffect(Unit) {
        socketEventListener.challengeUpdated.collect { event ->
            Log.d(TAG, "Challenge updated: ${event.message}")
            // Reload challenges to show the updated data
            challengesViewModel.loadChallenges()
        }
    }
    
    // Listen for new challenges created
    LaunchedEffect(Unit) {
        socketEventListener.challengeCreated.collect { event ->
            Log.d(TAG, "New challenge created: ${event.message}")
            // Reload challenges to show the new challenge
            challengesViewModel.loadChallenges()
        }
    }

    // Listen for challenges deleted
    LaunchedEffect(Unit) {
        socketEventListener.challengeDeleted.collect { event ->
            Log.d(TAG, "Challenge deleted: ${event.message}")
            // Reload challenges to show the deleted challenge
            challengesViewModel.loadChallenges()
        }
    }
    
    // Listen for users joining challenges
    LaunchedEffect(Unit) {
        socketEventListener.userJoinedChallenge.collect { event ->
            Log.d(TAG, "User joined challenge: ${event.message}")
            // Reload challenges to update member counts
            challengesViewModel.loadChallenges()
        }
    }
    
    // Listen for users leaving challenges
    LaunchedEffect(Unit) {
        socketEventListener.userLeftChallenge.collect { event ->
            Log.d(TAG, "User left challenge: ${event.message}")
            // Reload challenges to update member counts
            challengesViewModel.loadChallenges()
        }
    }

    // Listen for invitation declined
    LaunchedEffect(Unit) {
        socketEventListener.invitationDeclined.collect { event ->
            Log.d(TAG, "Invitation declined: ${event.message}")
            // Reload challenges to update invited users list
            challengesViewModel.loadChallenges()
        }
    }


    ChallengesContent(
        uiState = uiState,
        callbacks = ChallengesScreenCallbacks(
            onBackClick = actions.onBackClick,
            onAddChallengeClick = actions.onAddChallengeClick,
            onChallengeClick = actions.onChallengeClick
        )
    )

}

@Composable
private fun ChallengesContent(
    modifier: Modifier = Modifier,
    uiState: ChallengesUiState,
    callbacks: ChallengesScreenCallbacks
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ChallengesTopBar(
                onBackClick = { callbacks.onBackClick() }
            )
        }
    ) { paddingValues ->
        ChallengesBody(
            paddingValues = paddingValues,
            uiState = uiState,
            callbacks = callbacks,
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.challenges),
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
private fun ChallengesBody(
    paddingValues: PaddingValues,
    uiState: ChallengesUiState,
    callbacks: ChallengesScreenCallbacks,
    modifier: Modifier = Modifier

) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        // Use a local variable for smart casting
        val challenges = uiState.allChallenges

        when {
            // 1. Loading State
            uiState.isLoadingChallenges -> {
                CircularProgressIndicator()
            }

            // 2. Error State
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage!!,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 3. Success State (handle null and empty cases)


            (challenges != null) -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    challenges.forEach { (challengeType, challengeList) ->

                        item {
                            Text(
                                text = challengeType.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        if (challengeList.isEmpty()) {
                            item {
                                Text(
                                    text = "No ${challengeType} challenges available right now.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        } else {
                            items(challengeList) { challenge ->
                                ChallengeItem(
                                    challenge = challenge,
                                    onClick = { callbacks.onChallengeClick(challenge.id) }
                                )
                            }
                        }
                    }
                }
            }


            // 4. Initial state before loading has started (optional, but good practice)
            // This case is hit if isLoading is false, errorMessage is null, and challenges is null.
            else -> {
                Text("Welcome to challenges!")
            }
        }
        AddChallengeButton(
            onClick = {
                Log.d(TAG, "ChallengesBody hit")
                callbacks.onAddChallengeClick()
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp).width(200.dp).height(60.dp)

        )
    }

}

@Composable
fun ChallengeItem(challenge: Challenge, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = challenge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Member name list
                Column(modifier = Modifier.padding(start = 8.dp)) { // Indent the whole list
                    Text(
                        text = "Members:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium, // Make the title slightly bolder
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Check if there are any members to avoid showing an empty list
                    if (challenge.memberNames.isNotEmpty()) {
                        // Loop through each member and create a Text for each one
                        challenge.memberNames.forEach { memberName ->
                            Text(
                                text = "• $memberName", // Add a bullet point for style
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp) // Indent the names
                            )
                        }
                    } else {
                        Text(
                            text = "No members yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                modifier = Modifier.padding(start = 16.dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AddChallengeButton(
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Button(
        onClick = {
            Log.d(TAG, "AddChallengeButton: Button physically clicked.")
            onClick()
        },
        modifier = modifier,
    ) {
        Text("New Challenge")
    }

}

