package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import androidx.compose.animation.core.copy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.remote.dto.Challenge
import com.cpen321.usermanagement.data.remote.dto.CreateChallengeRequest
import com.cpen321.usermanagement.data.remote.dto.User
import com.cpen321.usermanagement.data.repository.ChallengesRepository
import com.cpen321.usermanagement.data.repository.FriendsRepository
import com.cpen321.usermanagement.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChallengesUiState(

    // loading states
    val isLoadingChallenges: Boolean = false,
    val isLoadingChallenge: Boolean = false,
    val isLoadingProfile: Boolean = false,
    val isLoadingFriends: Boolean = false,
    val isDeletingChallenge: Boolean = false,
    val isUpdatingChallenge: Boolean = false,


    //data states
    val user: User? = null,
    val allFriends: List<Friend>? = null,
    val allChallenges: Map<String, List<Challenge>>? = null,
    val allPendingChallenges: List<Challenge>? = null,
    val allActiveChallenges: List<Challenge>? = null,
    val allLiveChallenges: List<Challenge>? = null,
    val allFinishedChallenges: List<Challenge>? = null,
    val allCancelledChallenges: List<Challenge>? = null,
    val selectedChallenge: Challenge? = null,

    // message states
    val errorMessage: String? = null,
    val successMessage: String? = null,
)



@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val challengesRepository: ChallengesRepository,
    private val profileRepository: ProfileRepository,
    private val friendsRepository: FriendsRepository,
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

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingProfile = true, errorMessage = null)

            val profileResult = profileRepository.getProfile()

            val user = profileResult.getOrNull()



            _uiState.value = _uiState.value.copy(
                isLoadingProfile = false,
                user = user,
            )

            if (profileResult.isFailure) {
                val error = profileResult.exceptionOrNull()
                val errorMessage = error?.message ?: "Failed to load profile"
                Log.e(TAG, "Failed to load profile", error)

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

    fun loadFriends(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingFriends = true, errorMessage = null)

            val friendsResult = friendsRepository.getFriends(userId)
            val friends = friendsResult.getOrNull()

            _uiState.value = _uiState.value.copy(
                isLoadingFriends = false,
                allFriends = friends
            )

            if (friendsResult.isFailure) {
                val error = friendsResult.exceptionOrNull()
                val errorMessage = error?.message ?: "Failed to load friends"
                Log.e(TAG, "Failed to load friends", error)

                _uiState.value = _uiState.value.copy(
                    isLoadingFriends = false,
                    errorMessage = errorMessage
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingFriends = false,
                    errorMessage = null
                )
            }



        }
    }

    fun loadChallenges() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingChallenges = true, errorMessage = null)

            val challengesResult = challengesRepository.getChallenges()

            val challenges = challengesResult.getOrNull()

            _uiState.value = _uiState.value.copy(
                isLoadingChallenges = false,
                allChallenges = challenges,
                allPendingChallenges = challenges?.get("pending"),
                allActiveChallenges = challenges?.get("active"),
                allLiveChallenges = challenges?.get("live"),
                allFinishedChallenges = challenges?.get("finished"),
                allCancelledChallenges = challenges?.get("cancelled")
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

    fun loadChallenge(challengeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingChallenge = true, errorMessage = null)
            val challengeResult = challengesRepository.getChallenge(challengeId)
            val challenge = challengeResult.getOrNull()
            _uiState.value = _uiState.value.copy(
                isLoadingChallenge = false,
                selectedChallenge = challenge
            )
            if (challengeResult.isFailure) {
                val error = challengeResult.exceptionOrNull()
                val errorMessage = error?.message ?: "Failed to load challenge"
                Log.e(TAG, "Failed to load profile", error)

                _uiState.value = _uiState.value.copy(
                    isLoadingChallenges = false,
                    errorMessage = errorMessage
                )
            }


        }
    }

    fun createChallenge(challengeRequest: CreateChallengeRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingChallenges = true, errorMessage = null)

            Log.d(TAG, "Creating challenge with request: $challengeRequest")

            val challengesResult = challengesRepository.createChallenge(challengeRequest)

            _uiState.value = _uiState.value.copy(
                isLoadingChallenges = false,
            )
            loadChallenges()

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

    fun updateChallenge(challenge: Challenge) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingChallenge = true, errorMessage = null)

            val updatedChallengeResult = challengesRepository.updateChallenge(challenge)
            _uiState.value = _uiState.value.copy(
                isUpdatingChallenge = false,
            )
            loadChallenges()

            if (updatedChallengeResult.isFailure) {
                val error = updatedChallengeResult.exceptionOrNull()
                val errorMessage = error?.message ?: "Failed to update challenge"
                Log.e(TAG, "Failed to update challenge", error)

                _uiState.value = _uiState.value.copy(
                    isUpdatingChallenge = false,
                    errorMessage = errorMessage
                )
            }
        }
    }

    fun deleteChallenge(challengeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingChallenges = true, errorMessage = null)

            val deleteResult = challengesRepository.deleteChallenge(challengeId)

            _uiState.value = _uiState.value.copy(
                isLoadingChallenges = false,
                selectedChallenge = null, // Clear the selected challenge
                successMessage = "Challenge deleted successfully!"
            )


            if (deleteResult.isFailure) {
                val error = deleteResult.exceptionOrNull()
                val errorMessage = error?.message ?: "Failed to delete challenge"
                Log.e(TAG, "Failed to delete challenge", error)
                _uiState.value = _uiState.value.copy(
                    isLoadingChallenges = false,
                    errorMessage = errorMessage
                )
            }
        }
    }


}