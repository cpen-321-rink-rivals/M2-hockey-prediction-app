package com.cpen321.usermanagement.ui.screens

import Button
import Icon
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.cpen321.usermanagement.R
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.viewmodels.ProfileUiState
import com.cpen321.usermanagement.ui.viewmodels.ProfileViewModel



private data class ManageLanguagesSpokenScreenActions(
    val onBackClick: () -> Unit,
    val onLanguageToggle: (String) -> Unit,
    val onSaveClick: () -> Unit
)

private data class LanguagesFormData(
    val allLanguages: List<String>,
    val selectedLanguages: Set<String>,
    val onLanguageToggle: (String) -> Unit,
    val onSaveClick: () -> Unit
)
private data class ManageLanguagesScreenData(
    val uiState: ProfileUiState,
    val snackBarHostState: SnackbarHostState,
    val onSuccessMessageShown: () -> Unit,
    val onErrorMessageShown: () -> Unit
)

@Composable
fun ManageLanguagesSpokenScreen(
    profileViewModel: ProfileViewModel,
    onBackClick: () -> Unit,
) {
    // Remember previous state
    val uiState by profileViewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    // Side effects
    LaunchedEffect(Unit) {
        profileViewModel.clearSuccessMessage()
        profileViewModel.clearError()
        if (uiState.user == null) {
            profileViewModel.loadProfile()
        }
    }

    // Page content
    ManageLanguagesSpokenContent(
        data = ManageLanguagesScreenData(
            uiState = uiState,
            snackBarHostState = snackBarHostState,
            onSuccessMessageShown = profileViewModel::clearSuccessMessage,
            onErrorMessageShown = profileViewModel::clearError
        ),
        actions = ManageLanguagesSpokenScreenActions(
            onBackClick = onBackClick,
            onLanguageToggle = profileViewModel::toggleSpokenLanguage,
            onSaveClick = profileViewModel::saveSpokenLanguages
        ),
        isSaving = uiState.isSavingProfile,

        )

}

@Composable
private fun ManageLanguagesSpokenContent(
    data: ManageLanguagesScreenData,
    actions: ManageLanguagesSpokenScreenActions,
    isSaving: Boolean,
    modifier: Modifier = Modifier
    ) {

    // Scaffold gives a skeleton for a page with topbar, bottom bar and body
    Scaffold(
        modifier = modifier,
        topBar = {
            LanguagesSpokenTopBar(onBackClick = actions.onBackClick)
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LocalSpacing.current.large),
                contentAlignment = Alignment.Center
            ){
                SaveButton(
                    isSaving = isSaving,
                    onClick = actions.onSaveClick
                )
            }}

    ) { paddingValues ->
        ManageLanguagesSpokenBody(
            paddingValues = paddingValues,
            uiState = data.uiState,
            onLanguageToggle = actions.onLanguageToggle,
            onSaveClick = actions.onSaveClick
        )

    }

}
//
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagesSpokenTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.manage_languages_spoken),
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
private fun ManageLanguagesSpokenBody(
    paddingValues: PaddingValues,
    uiState: ProfileUiState,
    onLanguageToggle: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier

){
    val spacing = LocalSpacing.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when {
            uiState.isLoadingProfile -> {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LanguagesForm(
                    formData = LanguagesFormData(
                        allLanguages = uiState.allLanguages,
                        selectedLanguages = uiState.selectedLanguages,
                        onLanguageToggle = onLanguageToggle,
                        onSaveClick = onSaveClick
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(spacing.large)
                )
            }
        }

    }
}

@Composable
private fun LanguagesForm(
    formData: LanguagesFormData,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.large)
    ) {
        LanguagesSelectionCard(
            allLanguages = formData.allLanguages,
            selectedLanguages = formData.selectedLanguages,
            onLanguageToggle = formData.onLanguageToggle
        )


    }
}

@Composable
private fun LanguagesSelectionCard(
    allLanguages: List<String>,
    selectedLanguages: Set<String>,
    onLanguageToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.select_languages),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(spacing.medium))

            LanguagesGrid(
                allLanguages = allLanguages,
                selectedLanguages = selectedLanguages,
                onLanguageToggle = onLanguageToggle
            )
        }
    }
}

@Composable
private fun LanguagesGrid(
    allLanguages: List<String>,
    selectedLanguages: Set<String>,
    onLanguageToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    LazyVerticalGrid(
        modifier = modifier.fillMaxWidth(),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
        verticalArrangement = Arrangement.spacedBy(spacing.small)
    ) {
        items(
            items = allLanguages,
            key = { language -> language }
        ) { language ->
            LanguageChip(
                language = language,
                isSelected = selectedLanguages.contains(language),
                onClick = { onLanguageToggle(language) }
            )
        }
    }
}

@Composable
private fun LanguageChip(
    language: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        modifier = modifier.fillMaxWidth(),
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = language,
                maxLines = 1
            )
        },
        shape = CircleShape,
        leadingIcon = if (isSelected) {
            {
                Icon(
                    name = R.drawable.ic_check,
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun SaveButton(
    isSaving: Boolean,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current

    Button(
        onClick = onClick,
        enabled = !isSaving
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(spacing.medium),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(spacing.small))
        }
        Text(
            text = stringResource(
                if (isSaving) R.string.saving else R.string.save
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(modifier = modifier)
}