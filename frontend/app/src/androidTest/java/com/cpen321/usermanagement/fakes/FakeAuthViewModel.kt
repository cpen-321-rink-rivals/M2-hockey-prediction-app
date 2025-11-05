package com.cpen321.usermanagement.fakes

import androidx.lifecycle.ViewModel
import com.cpen321.usermanagement.ui.viewmodels.AuthUiState
import com.cpen321.usermanagement.ui.viewmodels.AuthViewModel
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.ui.viewmodels.AuthViewModelContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthViewModel : ViewModel(), AuthViewModelContract {
    private val _uiState = MutableStateFlow(
        AuthUiState(
            isCheckingAuth = false,
            user = User(
                _id = "currentUserId",
                email = "user@example.com",
                name = "Current User",
                bio = null,
                friendCode = "VALIDCODE",
                profilePicture = ""
            )
        )
    )

    override val uiState: StateFlow<AuthUiState> = _uiState
}