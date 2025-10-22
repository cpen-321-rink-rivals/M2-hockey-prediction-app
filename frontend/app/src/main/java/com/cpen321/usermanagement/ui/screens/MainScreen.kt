package com.cpen321.usermanagement.ui.screens

import Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.components.MessageSnackbar
import com.cpen321.usermanagement.ui.components.MessageSnackbarState
import com.cpen321.usermanagement.ui.viewmodels.MainUiState
import com.cpen321.usermanagement.ui.viewmodels.MainViewModel
import com.cpen321.usermanagement.ui.theme.LocalFontSizes
import com.cpen321.usermanagement.ui.theme.LocalSpacing



@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onProfileClick: () -> Unit,
    onTicketClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onChallengeClick: () -> Unit
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }


    // Side effects
    LaunchedEffect(Unit) {
        mainViewModel.clearSuccessMessage()
        if (uiState.user == null) {
            mainViewModel.loadProfile()
        }
    }

    MainContent(
        uiState = uiState,
        snackBarHostState = snackBarHostState,
        onProfileClick = onProfileClick,
        onTicketClick = onTicketClick,
        onFriendsClick = onFriendsClick,
        onChallengeClick = onChallengeClick,
        onSuccessMessageShown = mainViewModel::clearSuccessMessage
    )
}

@Composable
private fun MainContent(
    uiState: MainUiState,
    snackBarHostState: SnackbarHostState,
    onProfileClick: () -> Unit,
    onTicketClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onChallengeClick: () -> Unit,
    onSuccessMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MainTopBar(onProfileClick = onProfileClick, onTicketClick = onTicketClick, onFriendsClick = onFriendsClick, onChallengeClick = onChallengeClick)
            
        },
        snackbarHost = {
            MainSnackbarHost(
                hostState = snackBarHostState,
                successMessage = uiState.successMessage,
                onSuccessMessageShown = onSuccessMessageShown
            )
        }
    ) { paddingValues ->
        MainBody(
            paddingValues = paddingValues,
            uiState = uiState
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    onProfileClick: () -> Unit,
    onTicketClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onChallengeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            AppTitle()
        },
        actions = {
            BingoTicketActionButton(onClick = onTicketClick)
            ChallengeActionButton(onClick = onChallengeClick)

            FriendsActionButton(onClick = onFriendsClick)

            ProfileActionButton(onClick = onProfileClick)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun AppTitle(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Composable
private fun ProfileActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        ProfileIcon()
    }
}

@Composable
private fun BingoTicketActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        BingoTicketIcon(onClick = onClick)
    }
}

@Composable
private fun ChallengeActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        ChallengeIcon(onClick = onClick)
    }
}

@Composable
private fun FriendsActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        Icon(
            name = R.drawable.ic_heart_smile, // or another icon you prefer
        )
    }
}

@Composable
private fun ProfileIcon() {
    Icon(
        name = R.drawable.ic_account_circle,
    )
}

@Composable
private fun ChallengeIcon(onClick: () -> Unit) {
    Icon(
        name = R.drawable.swords_icon,
    )
}

@Composable
private fun BingoTicketIcon(onClick: () -> Unit) {
    Icon(
        name = R.drawable.bingo_ticket,
    )
}

@Composable
private fun MainSnackbarHost(
    hostState: SnackbarHostState,
    successMessage: String?,
    onSuccessMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    MessageSnackbar(
        hostState = hostState,
        messageState = MessageSnackbarState(
            successMessage = successMessage,
            errorMessage = null,
            onSuccessMessageShown = onSuccessMessageShown,
            onErrorMessageShown = { }
        ),
        modifier = modifier
    )
}

@Composable
private fun MainBody(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    uiState: MainUiState,

    ) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        WelcomeMessage(uiState = uiState)
    }
}

@Composable
private fun WelcomeMessage(
    modifier: Modifier = Modifier,
    uiState: MainUiState, // Add this parameter

) {
    val fontSizes = LocalFontSizes.current

    val welcomeText = uiState.user?.let {
        stringResource(R.string.put_your_knowledge_to_the_test) + ", " + uiState.user.name
    } ?: stringResource(R.string.welcome)

    Text(
        text = welcomeText,
        style = MaterialTheme.typography.bodyLarge,
        fontSize = fontSizes.extraLarge3,
        lineHeight = 40.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(10.dp)
    )
}