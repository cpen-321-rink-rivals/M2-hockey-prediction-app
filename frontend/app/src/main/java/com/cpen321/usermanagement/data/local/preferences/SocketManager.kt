package com.cpen321.usermanagement.data.local.preferences

import android.util.Log
import com.cpen321.usermanagement.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Socket.IO connection for real-time communication with the backend
 */
@Singleton
class SocketManager @Inject constructor() {

    companion object {
        private const val TAG = "SocketManager"
        // SOCKET_URL is configured in local.properties
        private val SERVER_URL = BuildConfig.SOCKET_URL
    }

    private var socket: Socket? = null

    // Connection state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Authenticated state
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    /**
     * Initialize and connect to the Socket.IO server
     */
    fun connect() {
        if (socket?.connected() == true) {
            Log.d(TAG, "Already connected")
            return
        }

        try {
            val opts = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                timeout = 20000
            }

            socket = IO.socket(SERVER_URL, opts)

            // Set up connection event listeners
            setupConnectionListeners()

            // Connect
            socket?.connect()
            Log.d(TAG, "Connecting to $SERVER_URL...")

        } catch (e: URISyntaxException) {
            Log.e(TAG, "Invalid server URL: $SERVER_URL", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating socket", e)
        }
    }

    /**
     * Disconnect from the Socket.IO server
     */
    fun disconnect() {
        socket?.disconnect()
        socket?.off() // Remove all listeners
        _isConnected.value = false
        _isAuthenticated.value = false
        Log.d(TAG, "Disconnected from server")
    }

    /**
     * Authenticate the socket connection with user credentials
     */
    fun authenticate(userId: String, userEmail: String? = null) {
        if (socket?.connected() != true) {
            Log.w(TAG, "Cannot authenticate: not connected")
            return
        }

        val authData = JSONObject().apply {
            put("userId", userId)
            userEmail?.let { put("userEmail", it) }
        }

        socket?.emit("authenticate", authData)
        Log.d(TAG, "Authenticating user: $userId")
    }

    /**
     * Join a challenge room to receive updates for that challenge
     */
    fun joinChallengeRoom(challengeId: String) {
        if (socket?.connected() != true) {
            Log.w(TAG, "Cannot join room: not connected")
            return
        }

        socket?.emit("join_challenge", challengeId)
        Log.d(TAG, "Joining challenge room: $challengeId")
    }

    /**
     * Leave a challenge room
     */
    fun leaveChallengeRoom(challengeId: String) {
        if (socket?.connected() != true) {
            Log.w(TAG, "Cannot leave room: not connected")
            return
        }

        socket?.emit("leave_challenge", challengeId)
        Log.d(TAG, "Leaving challenge room: $challengeId")
    }

    /**
     * Listen for a specific event
     */
    fun on(event: String, listener: Emitter.Listener) {
        socket?.on(event, listener)
    }

    /**
     * Stop listening for a specific event
     */
    fun off(event: String) {
        socket?.off(event)
    }

    /**
     * Emit a custom event to the server
     */
    fun emit(event: String, data: Any) {
        socket?.emit(event, data)
    }

    /**
     * Set up connection event listeners
     */
    private fun setupConnectionListeners() {
        socket?.on(Socket.EVENT_CONNECT) {
            Log.d(TAG, "✅ Connected to server")
            _isConnected.value = true
        }

        socket?.on(Socket.EVENT_DISCONNECT) {
            Log.d(TAG, "❌ Disconnected from server")
            _isConnected.value = false
            _isAuthenticated.value = false
        }

        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e(TAG, "Connection error: ${args.getOrNull(0)}")
            _isConnected.value = false
        }

//        socket?.on(Socket.EVENT_RECONNECT) { args ->
//            Log.d(TAG, "Reconnected after ${args.getOrNull(0)} attempts")
//            _isConnected.value = true
//        }
//
//        socket?.on(Socket.EVENT_RECONNECT_ATTEMPT) {
//            Log.d(TAG, "Attempting to reconnect...")
//        }
//
//        socket?.on(Socket.EVENT_RECONNECT_ERROR) { args ->
//            Log.e(TAG, "Reconnection error: ${args.getOrNull(0)}")
//        }
//
//        socket?.on(Socket.EVENT_RECONNECT_FAILED) {
//            Log.e(TAG, "Failed to reconnect")
//        }

        // Handle authentication confirmation
        socket?.on("authenticated") { args ->
            val data = args.getOrNull(0) as? JSONObject
            val userId = data?.optString("userId")
            Log.d(TAG, "✅ Authenticated as user: $userId")
            _isAuthenticated.value = true
        }

        // Handle generic errors
        socket?.on("error") { args ->
            val data = args.getOrNull(0) as? JSONObject
            val message = data?.optString("message") ?: "Unknown error"
            Log.e(TAG, "Server error: $message")
        }
    }

    /**
     * Check if currently connected
     */
    fun isConnected(): Boolean = socket?.connected() == true

    /**
     * Get the socket instance for advanced usage
     */
    fun getSocket(): Socket? = socket
}