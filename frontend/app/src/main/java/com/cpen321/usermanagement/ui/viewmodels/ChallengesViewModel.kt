package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.Challenge
import com.cpen321.usermanagement.data.repository.ChallengesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChallengesUiState(
    val isLoadingChallenges: Boolean = false,

    val allChallenges: List<Challenge>? = null,

    val errorMessage: String? = null,
    val successMessage: String? = null,
)



@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val challengesRepository: ChallengesRepository
    // Inject repositories here if needed in the future
) : ViewModel() {
    companion object {
        private const val TAG = "ChallengesViewModel"
    }

    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState = _uiState.asStateFlow()

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun loadChallenges() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingChallenges = true, errorMessage = null)

            val challengesResult = challengesRepository.getChallenges()

            val challenges = challengesResult.getOrNull()

            _uiState.value = _uiState.value.copy(
                isLoadingChallenges = false,
                allChallenges = challenges
            )

            if (challengesResult.isFailure) {
                val error = challengesResult.exceptionOrNull()
                val errorMessage = error?.message ?: "Failed to load challenges"
                Log.e(TAG, "Failed to load profile", error)

                _uiState.value = _uiState.value.copy(
                    isLoadingChallenges = false,
                    errorMessage = errorMessage
                )
            }






        }
    }
}