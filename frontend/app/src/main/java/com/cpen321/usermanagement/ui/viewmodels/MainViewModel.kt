package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(

    // Loading messages
    val isLoadingProfile: Boolean = false,

    // Data states
    val user: User? = null,
    val allHobbies: List<String> = emptyList(),
    val allLanguages: List<String> = emptyList(),
    val selectedHobbies: Set<String> = emptySet(),
    val selectedLanguages: Set<String> = emptySet(),

    // Message states
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "MainViewModel"
    }

    fun setSuccessMessage(message: String) {
        _uiState.value = _uiState.value.copy(successMessage = message)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingProfile = true, errorMessage = null)

            val profileResult = profileRepository.getProfile()
            val hobbiesResult = profileRepository.getAvailableHobbies()
            val languagesSpokenResult = profileRepository.getAvailableLanguages()

            val user = profileResult.getOrNull()

            val availableHobbies = hobbiesResult.getOrNull()
            val availableSpokenLanguages = languagesSpokenResult.getOrNull()

            val selectedHobbies = user?.hobbies?.toSet()
            val selectedLanguages = user?.languages?.toSet()



            _uiState.value = _uiState.value.copy(
                isLoadingProfile = false,
                user = user,

                allHobbies = availableHobbies ?: _uiState.value.allHobbies,
                selectedHobbies = selectedHobbies ?: _uiState.value.selectedHobbies,

                allLanguages = availableSpokenLanguages ?: _uiState.value.allLanguages,
                selectedLanguages = selectedLanguages ?: _uiState.value.selectedLanguages
            )

            if (profileResult.isFailure) {
                val error = profileResult.exceptionOrNull()
                val errorMessage = error?.message ?: "Failed to load profile"
                Log.e(TAG, "Failed to load profile", error)

                _uiState.value = _uiState.value.copy(
                    isLoadingProfile = false,
                    errorMessage = errorMessage
                )
            } else if (hobbiesResult.isFailure) {
                val error = hobbiesResult.exceptionOrNull()
                val errorMessage = error?.message ?: "Failed to load hobbies"
                Log.e(TAG, "Failed to load hobbies", error)

                _uiState.value = _uiState.value.copy(
                    isLoadingProfile = false,
                    errorMessage = errorMessage
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingProfile = false,
                    errorMessage = null
                )
            }
        }
    }
}
