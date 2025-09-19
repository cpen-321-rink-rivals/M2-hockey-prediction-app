package com.cpen321.usermanagement.ui.viewmodels

import android.net.Uri
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

data class ProfileUiState(
    // Loading states
    val isLoadingProfile: Boolean = false,
    val isSavingProfile: Boolean = false,
    val isLoadingPhoto: Boolean = false,

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
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

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

    fun toggleHobby(hobby: String) {
        val currentSelected = _uiState.value.selectedHobbies.toMutableSet()
        if (currentSelected.contains(hobby)) {
            currentSelected.remove(hobby)
        } else {
            currentSelected.add(hobby)
        }
        _uiState.value = _uiState.value.copy(selectedHobbies = currentSelected)
    }

    fun saveHobbies() {
        viewModelScope.launch {
            val originalHobbies = _uiState.value.user?.hobbies?.toSet() ?: emptySet()

            _uiState.value =
                _uiState.value.copy(
                    isSavingProfile = true,
                    errorMessage = null,
                    successMessage = null
                )

            val selectedHobbiesList = _uiState.value.selectedHobbies.toList()
            val result = profileRepository.updateProfile(hobbies = selectedHobbiesList)

            if (result.isSuccess) {
                val updatedUser = result.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    isSavingProfile = false,
                    user = updatedUser,
                    successMessage = "Hobbies updated successfully!"
                )
            } else {
                // Revert to original hobbies on failure
                val error = result.exceptionOrNull()
                Log.d(TAG, "error: $error")
                Log.e(TAG, "Failed to update hobbies", error)
                val errorMessage = error?.message ?: "Failed to update hobbies"

                _uiState.value = _uiState.value.copy(
                    isSavingProfile = false,
                    selectedHobbies = originalHobbies, // Revert the selected hobbies
                    errorMessage = errorMessage
                )
            }
        }
    }

    fun toggleSpokenLanguage(language: String) {
        val currentSelected = _uiState.value.selectedLanguages.toMutableSet()
        if (currentSelected.contains(language)) {
            currentSelected.remove(language)
        } else {
            currentSelected.add(language)
        }
        _uiState.value = _uiState.value.copy(selectedLanguages = currentSelected)
    }

    fun saveSpokenLanguages() {
        viewModelScope.launch {
            val originalSpokenLanguages = _uiState.value.user?.languages?.toSet() ?: emptySet()

            _uiState.value =
                _uiState.value.copy(
                    isSavingProfile = true,
                    errorMessage = null,
                    successMessage = null
                )

            val selectedSpokenLanguagesList = _uiState.value.selectedLanguages.toList()
            val result = profileRepository.updateProfile(languages = selectedSpokenLanguagesList)

            if (result.isSuccess) {
                val updatedUser = result.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    isSavingProfile = false,
                    user = updatedUser,
                    successMessage = "SpokenLanguages updated successfully!"
                )
            } else {
                // Revert to original SpokenLanguages on failure
                val error = result.exceptionOrNull()
                Log.d(TAG, "error: $error")
                Log.e(TAG, "Failed to update SpokenLanguages", error)
                val errorMessage = error?.message ?: "Failed to update SpokenLanguages"

                _uiState.value = _uiState.value.copy(
                    isSavingProfile = false,
                    selectedLanguages = originalSpokenLanguages, // Revert the selected SpokenLanguages
                    errorMessage = errorMessage
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun setLoadingPhoto(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoadingPhoto = isLoading)
    }

    fun uploadProfilePicture(pictureUri: Uri) {
        viewModelScope.launch {
            val currentUser = _uiState.value.user ?: return@launch
            val updatedUser = currentUser.copy(profilePicture = pictureUri.toString())
            _uiState.value = _uiState.value.copy(isLoadingPhoto = false, user= updatedUser, successMessage = "Profile picture updated successfully!")
        }
    }

    fun updateProfile(name: String, bio: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    isSavingProfile = true,
                    errorMessage = null,
                    successMessage = null
                )

            val result = profileRepository.updateProfile(name, bio)
            if (result.isSuccess) {
                val updatedUser = result.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    isSavingProfile = false,
                    user = updatedUser,
                    successMessage = "Profile updated successfully!"
                )
                onSuccess()
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "Failed to update profile", error)
                val errorMessage = error?.message ?: "Failed to update profile"
                _uiState.value = _uiState.value.copy(
                    isSavingProfile = false,
                    errorMessage = errorMessage
                )
            }
        }
    }
}
