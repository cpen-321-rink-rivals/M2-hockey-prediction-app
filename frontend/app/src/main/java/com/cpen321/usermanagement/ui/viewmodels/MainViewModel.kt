package com.cpen321.usermanagement.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.usermanagement.data.local.preferences.NhlDataManager
import com.cpen321.usermanagement.data.local.preferences.SocketManager
import com.cpen321.usermanagement.data.local.preferences.SocketEventListener
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
    private val profileRepository: ProfileRepository,
    private val nhlDataManager: NhlDataManager,
    val socketManager: SocketManager,
    val socketEventListener: SocketEventListener
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Expose NHL data from manager
    val nhlDataState = nhlDataManager.uiState
    
    // Expose socket connection state
    val socketConnected = socketManager.isConnected
    val socketAuthenticated = socketManager.isAuthenticated

    companion object {
        private const val TAG = "MainViewModel"
    }
    
    init {
        // Connect socket when ViewModel is created
        connectSocket()
        // Start listening for events
        socketEventListener.startListening()
    }
    
    /**
     * Connect to WebSocket server
     */
    fun connectSocket() {
        Log.d(TAG, "Connecting to WebSocket server")
        socketManager.connect()
    }
    
    /**
     * Authenticate socket connection with user info
     */
    fun authenticateSocket(userId: String, userEmail: String?) {
        Log.d(TAG, "Authenticating socket for user: $userId")
        socketManager.authenticate(userId, userEmail)
    }
    
    /**
     * Disconnect from WebSocket server
     */
    fun disconnectSocket() {
        Log.d(TAG, "Disconnecting from WebSocket server")
        socketManager.disconnect()
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up socket connection when ViewModel is destroyed
        socketEventListener.stopListening()
        socketManager.disconnect()
    }

    fun setSuccessMessage(message: String) {
        _uiState.value = _uiState.value.copy(successMessage = message)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun loadGameSchedule(){
        viewModelScope.launch {
            nhlDataManager.loadSchedule()
        }
    }

    fun clearScheduleError() {
        nhlDataManager.clearError()
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
            val selectedLanguages = user?.languagesSpoken?.toSet()



            _uiState.value = _uiState.value.copy(
                isLoadingProfile = false,
                user = user,

                allHobbies = availableHobbies ?: _uiState.value.allHobbies,
                selectedHobbies = selectedHobbies ?: _uiState.value.selectedHobbies,

                allLanguages = availableSpokenLanguages ?: _uiState.value.allLanguages,
                selectedLanguages = selectedLanguages ?: _uiState.value.selectedLanguages
            )
            
            // Authenticate socket with user info after successful profile load
            if (user != null) {
                authenticateSocket(user._id, user.email)
            }

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
