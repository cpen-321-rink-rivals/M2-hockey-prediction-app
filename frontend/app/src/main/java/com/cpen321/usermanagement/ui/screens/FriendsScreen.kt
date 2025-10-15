package com.cpen321.usermanagement.ui.screens

import Icon
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.viewmodels.AuthViewModel
import com.cpen321.usermanagement.ui.viewmodels.FriendsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    authViewModel: AuthViewModel,
    viewModel: FriendsViewModel,
    onBackClick: () -> Unit
) {

    // Use the unified UI state
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    if (authState.isCheckingAuth || authState.user == null) {
        // show a loading indicator or blank screen
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentUserId = authState.user?._id

    LaunchedEffect(currentUserId) {
        currentUserId?.let {
            viewModel.setCurrentUser(it)
        }
    }

    var friendCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
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
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Add Friend Section
            OutlinedTextField(
                value = friendCode,
                onValueChange = { friendCode = it },
                label = { Text("Enter friend code") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.sendFriendRequest(friendCode)
                    friendCode = ""
                },
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
            ) {
                Text("Send Request")
            }

            // Show status message for a few seconds
            uiState.statusMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pending Requests
            if (uiState.pendingRequests.isNotEmpty()) {
                Text("Pending Requests", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(uiState.pendingRequests) { friend ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(friend.sender.name)
                            Row {
                                TextButton(onClick = { viewModel.acceptFriendRequest(friend._id) }) {
                                    Text("Accept")
                                }
                                TextButton(onClick = { viewModel.rejectFriendRequest(friend._id) }) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))


            // Friend List
            Text("Friends", style = MaterialTheme.typography.titleMedium)
            if (uiState.friends.isEmpty()) {
                Text("No friends yet.")
            } else {
                LazyColumn {
                    items(uiState.friends) { friend ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(friend.name)
                            TextButton(onClick = { viewModel.removeFriend(friend.id) }) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}
