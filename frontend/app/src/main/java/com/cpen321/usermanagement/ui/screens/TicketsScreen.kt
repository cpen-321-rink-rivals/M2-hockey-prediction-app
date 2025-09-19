package com.cpen321.usermanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.cpen321.usermanagement.ui.theme.UserManagementTheme // Assuming you have a theme
import com.cpen321.usermanagement.ui.viewmodels.TicketsViewModel
import com.cpen321.usermanagement.ui.viewmodels.TicketsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(
    viewModel: TicketsViewModel = hiltViewModel(),
    onNavigateBack: (() -> Unit)? = null // Optional: for back navigation
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tickets") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // navigationIcon = { // If you need a back button
                //     if (onNavigateBack != null) {
                //         IconButton(onClick = onNavigateBack) {
                //             Icon(
                //                 imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                //                 contentDescription = "Back"
                //             )
                //         }
                //     }
                // }
            )
        }
    ) { paddingValues ->
        TicketsContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState
        )
    }
}

@Composable
fun TicketsContent(
    modifier: Modifier = Modifier,
    uiState: TicketsUiState
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.isLoading) {
            // CircularProgressIndicator() // Show a loader if needed
            Text("Loading tickets...")
        } else if (uiState.errorMessage != null) {
            Text("Error: ${uiState.errorMessage}")
        } else {
            // TODO: Replace with your actual tickets list UI
            Text(
                text = "No tickets yet.",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(text = "This is where your tickets will be displayed.")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TicketsScreenPreview() {
    UserManagementTheme { // Apply your app's theme for the preview
        TicketsScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun TicketsContentPreview() {
    UserManagementTheme {
        TicketsContent(uiState = TicketsUiState(isLoading = false))
    }
}

@Preview(showBackground = true)
@Composable
fun TicketsContentLoadingPreview() {
    UserManagementTheme {
        TicketsContent(uiState = TicketsUiState(isLoading = true))
    }
}
