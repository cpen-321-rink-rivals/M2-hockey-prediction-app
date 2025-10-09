package com.cpen321.usermanagement.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import com.cpen321.usermanagement.data.remote.dto.Challenge
import com.cpen321.usermanagement.ui.viewmodels.ChallengesUiState


private data class ChallengesScreenCallbacks(
    val onBackClick: () -> Unit
)

data class ChallengesScreenActions(
    val onBackClick: () -> Unit
)



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
            onBackClick = actions.onBackClick
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
        )
    }

}

@Composable
private fun ChallengesBody(
    paddingValues: PaddingValues,
    uiState: ChallengesUiState,
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
                            ChallengeItem(challenge = challenge)
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
    }

}

@Composable
fun ChallengeItem(challenge: Challenge) {
    // TODO: Example of how to display a Challenge. Adjust based on your Challenge data class properties
    Text(
        text = challenge.title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ChallengesTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {

}
