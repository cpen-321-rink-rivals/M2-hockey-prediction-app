package com.cpen321.usermanagement.ui.screens

import Icon
import android.graphics.drawable.Icon
import android.util.Log
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
import com.cpen321.usermanagement.R
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
    actions: ChallengesScreenActions

) {

    val uiState by challengesViewModel.uiState.collectAsState()

    // Side effects
    LaunchedEffect(Unit) {
        challengesViewModel.clearSuccessMessage()
        challengesViewModel.clearError()

        challengesViewModel.loadChallenges()

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
            challenges != null -> {
                if (challenges.isEmpty()) {
                    // Success, but the list is empty
                    Text("No challenges available right now.")
                } else {
                    // Success, and there are challenges to display
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(challenges) { challenge ->
                            // Replace with your actual ChallengeItem Composable
                            ChallengeItem(
                                challenge = challenge,
                                onClick = { callbacks.onChallengeClick(challenge.id)
                                }
                            )
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

            // 4. Initial state before loading has started (optional, but good practice)
            // This case is hit if isLoading is false, errorMessage is null, and challenges is null.
            else -> {
                Text("Welcome to challenges!")
            }
        }
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
                Text(
                    text = "By ${challenge.ownerId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
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

