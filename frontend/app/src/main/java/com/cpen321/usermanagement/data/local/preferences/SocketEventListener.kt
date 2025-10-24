package com.cpen321.usermanagement.data.local.preferences

import android.util.Log
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data classes for socket events
 */
data class ChallengeEvent(
    val type: String,
    val challengeId: String?,
    val message: String,
    val data: JSONObject?
)

data class NotificationEvent(
    val message: String,
    val data: JSONObject?
)

/**
 * Listens for socket events and exposes them as Kotlin Flows
 */
@Singleton
class SocketEventListener @Inject constructor(
    private val socketManager: SocketManager
) {
    
    companion object {
        private const val TAG = "SocketEventListener"
    }

    // Challenge events
    private val _challengeInvitations = MutableSharedFlow<ChallengeEvent>()
    val challengeInvitations: SharedFlow<ChallengeEvent> = _challengeInvitations.asSharedFlow()

    private val _challengeCreated = MutableSharedFlow<ChallengeEvent>()
    val challengeCreated: SharedFlow<ChallengeEvent> = _challengeCreated.asSharedFlow()

    private val _challengeUpdated = MutableSharedFlow<ChallengeEvent>()
    val challengeUpdated: SharedFlow<ChallengeEvent> = _challengeUpdated.asSharedFlow()

    private val _userJoinedChallenge = MutableSharedFlow<ChallengeEvent>()
    val userJoinedChallenge: SharedFlow<ChallengeEvent> = _userJoinedChallenge.asSharedFlow()

    private val _userLeftChallenge = MutableSharedFlow<ChallengeEvent>()
    val userLeftChallenge: SharedFlow<ChallengeEvent> = _userLeftChallenge.asSharedFlow()

    private val _challengeStatusChanged = MutableSharedFlow<ChallengeEvent>()
    val challengeStatusChanged: SharedFlow<ChallengeEvent> = _challengeStatusChanged.asSharedFlow()

    // Friend events
    private val _friendRequestReceived = MutableSharedFlow<NotificationEvent>()
    val friendRequestReceived: SharedFlow<NotificationEvent> = _friendRequestReceived.asSharedFlow()

    private val _friendRequestAccepted = MutableSharedFlow<NotificationEvent>()
    val friendRequestAccepted: SharedFlow<NotificationEvent> = _friendRequestAccepted.asSharedFlow()

    // System events
    private val _systemAnnouncement = MutableSharedFlow<NotificationEvent>()
    val systemAnnouncement: SharedFlow<NotificationEvent> = _systemAnnouncement.asSharedFlow()

    private val _notifications = MutableSharedFlow<NotificationEvent>()
    val notifications: SharedFlow<NotificationEvent> = _notifications.asSharedFlow()

    /**
     * Start listening for all socket events
     */
    fun startListening() {
        Log.d(TAG, "Starting socket event listeners")

        // Challenge invitation
        socketManager.on("challenge_invitation", onChallengeInvitation)
        
        // Challenge created
        socketManager.on("challenge_created", onChallengeCreated)
        
        // Challenge updated
        socketManager.on("challenge_updated", onChallengeUpdated)
        
        // User joined challenge
        socketManager.on("user_joined_challenge", onUserJoinedChallenge)
        
        // User left challenge
        socketManager.on("user_left_challenge", onUserLeftChallenge)
        
        // Challenge status changed
        socketManager.on("challenge_status_changed", onChallengeStatusChanged)
        
        // Friend request received
        socketManager.on("friend_request_received", onFriendRequestReceived)
        
        // Friend request accepted
        socketManager.on("friend_request_accepted", onFriendRequestAccepted)
        
        // System announcement
        socketManager.on("system_announcement", onSystemAnnouncement)
        
        // Generic notification
        socketManager.on("notification", onNotification)
    }

    /**
     * Stop listening for all socket events
     */
    fun stopListening() {
        Log.d(TAG, "Stopping socket event listeners")
        
        socketManager.off("challenge_invitation")
        socketManager.off("challenge_created")
        socketManager.off("challenge_updated")
        socketManager.off("user_joined_challenge")
        socketManager.off("user_left_challenge")
        socketManager.off("challenge_status_changed")
        socketManager.off("friend_request_received")
        socketManager.off("friend_request_accepted")
        socketManager.off("system_announcement")
        socketManager.off("notification")
    }

    // Event listeners
    private val onChallengeInvitation = Emitter.Listener { args ->
        parseChallengeEvent(args, _challengeInvitations, "challenge_invitation")
    }

    private val onChallengeCreated = Emitter.Listener { args ->
        parseChallengeEvent(args, _challengeCreated, "challenge_created")
    }

    private val onChallengeUpdated = Emitter.Listener { args ->
        parseChallengeEvent(args, _challengeUpdated, "challenge_updated")
    }

    private val onUserJoinedChallenge = Emitter.Listener { args ->
        parseChallengeEvent(args, _userJoinedChallenge, "user_joined_challenge")
    }

    private val onUserLeftChallenge = Emitter.Listener { args ->
        parseChallengeEvent(args, _userLeftChallenge, "user_left_challenge")
    }

    private val onChallengeStatusChanged = Emitter.Listener { args ->
        parseChallengeEvent(args, _challengeStatusChanged, "challenge_status_changed")
    }

    private val onFriendRequestReceived = Emitter.Listener { args ->
        parseNotificationEvent(args, _friendRequestReceived, "friend_request_received")
    }

    private val onFriendRequestAccepted = Emitter.Listener { args ->
        parseNotificationEvent(args, _friendRequestAccepted, "friend_request_accepted")
    }

    private val onSystemAnnouncement = Emitter.Listener { args ->
        parseNotificationEvent(args, _systemAnnouncement, "system_announcement")
    }

    private val onNotification = Emitter.Listener { args ->
        parseNotificationEvent(args, _notifications, "notification")
    }

    // Helper functions to parse events
    private fun parseChallengeEvent(
        args: Array<Any>,
        flow: MutableSharedFlow<ChallengeEvent>,
        eventName: String
    ) {
        try {
            val data = args.getOrNull(0) as? JSONObject
            if (data != null) {
                val event = ChallengeEvent(
                    type = data.optString("type", eventName),
                    challengeId = data.optJSONObject("challenge")?.optString("id"),
                    message = data.optString("message", ""),
                    data = data
                )
                
                Log.d(TAG, "Received $eventName: ${event.message}")
                
                // Emit to flow (this is suspending, but Emitter.Listener runs in IO thread)
                runBlocking {
                    flow.emit(event)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing $eventName", e)
        }
    }

    private fun parseNotificationEvent(
        args: Array<Any>,
        flow: MutableSharedFlow<NotificationEvent>,
        eventName: String
    ) {
        try {
            val data = args.getOrNull(0) as? JSONObject
            if (data != null) {
                val event = NotificationEvent(
                    message = data.optString("message", ""),
                    data = data
                )
                
                Log.d(TAG, "Received $eventName: ${event.message}")

                runBlocking {
                    flow.emit(event)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing $eventName", e)
        }
    }
}
