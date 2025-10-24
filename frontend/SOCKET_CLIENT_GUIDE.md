# Socket.IO Client Implementation - Android Frontend

## 📁 File Structure

```
frontend/app/src/main/java/com/cpen321/usermanagement/
├── data/
│   └── local/
│       └── socket/
│           ├── SocketManager.kt          ← Core socket connection manager
│           └── SocketEventListener.kt    ← Listens for socket events
└── ui/
    └── viewmodels/
        └── MainViewModel.kt              ← Connects socket on app start
```

## 🔌 Where Socket Connects

### **Answer: In `MainViewModel.kt`**

The socket connection is initialized in the `MainViewModel` which is created when the app starts:

```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val socketManager: SocketManager,
    private val socketEventListener: SocketEventListener
    // ...
) : ViewModel() {

    init {
        // ✅ Socket connects HERE when MainViewModel is created
        connectSocket()
        socketEventListener.startListening()
    }

    fun connectSocket() {
        socketManager.connect()
    }
}
```

## 📍 Connection Flow

### 1. **App Starts** → MainViewModel created

- File: `MainViewModel.kt`
- Happens: Automatically via Hilt dependency injection
- When: When `MainActivity` is launched

### 2. **init {} block runs** → Socket connects

```kotlin
init {
    connectSocket()                      // Connects to ws://10.0.2.2:3000
    socketEventListener.startListening() // Starts listening for events
}
```

### 3. **User logs in** → Socket authenticates

```kotlin
// After Google Sign-In succeeds:
mainViewModel.authenticateSocket(userId, userEmail)
```

### 4. **User views challenge** → Joins room

```kotlin
// When ChallengeDetailsScreen opens:
socketManager.joinChallengeRoom(challengeId)
```

### 5. **User leaves challenge** → Leaves room

```kotlin
// When ChallengeDetailsScreen closes:
socketManager.leaveChallengeRoom(challengeId)
```

## 🎯 Key Components

### 1. **SocketManager** (`SocketManager.kt`)

**Purpose:** Manages the Socket.IO connection

**Key Methods:**

- `connect()` - Connect to server
- `disconnect()` - Disconnect from server
- `authenticate(userId, email)` - Authenticate user
- `joinChallengeRoom(id)` - Join challenge room
- `leaveChallengeRoom(id)` - Leave challenge room
- `emit(event, data)` - Send custom event

**State:**

- `isConnected: StateFlow<Boolean>` - Connection status
- `isAuthenticated: StateFlow<Boolean>` - Auth status

---

### 2. **SocketEventListener** (`SocketEventListener.kt`)

**Purpose:** Listens for events from server and exposes them as Kotlin Flows

**Available Events (Flows):**

- `challengeInvitations` - When invited to challenge
- `challengeCreated` - When your challenge is created
- `challengeUpdated` - When challenge is modified
- `userJoinedChallenge` - When someone joins
- `userLeftChallenge` - When someone leaves
- `challengeStatusChanged` - When status changes (pending → live, etc.)
- `friendRequestReceived` - New friend request
- `friendRequestAccepted` - Friend accepted your request
- `systemAnnouncement` - System-wide messages
- `notifications` - Generic notifications

---

### 3. **MainViewModel** (`MainViewModel.kt`)

**Purpose:** App-level ViewModel that manages socket lifecycle

**Responsibilities:**

- Connects socket on app start (in `init {}`)
- Exposes socket state to UI
- Authenticates socket after login
- Disconnects socket on cleanup

**Exposed State:**

```kotlin
val socketConnected: StateFlow<Boolean>      // true if connected
val socketAuthenticated: StateFlow<Boolean>  // true if authenticated
```

## 📱 How to Use in Screens

### Example 1: Listen for Challenge Invitations

```kotlin
@Composable
fun ChallengesScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    socketEventListener: SocketEventListener = hiltViewModel() // Inject via Hilt
) {
    // Collect socket events
    LaunchedEffect(Unit) {
        socketEventListener.challengeInvitations.collect { event ->
            // Show notification
            showToast("New invitation: ${event.message}")

            // Refresh challenges list
            refreshChallenges()
        }
    }

    // Your UI code...
}
```

### Example 2: Join Challenge Room

```kotlin
@Composable
fun ChallengeDetailsScreen(
    challengeId: String,
    socketManager: SocketManager = hiltViewModel() // Inject via Hilt
) {
    // Join room when screen opens
    LaunchedEffect(challengeId) {
        socketManager.joinChallengeRoom(challengeId)
    }

    // Leave room when screen closes
    DisposableEffect(challengeId) {
        onDispose {
            socketManager.leaveChallengeRoom(challengeId)
        }
    }

    // Your UI code...
}
```

### Example 3: Show Connection Status

```kotlin
@Composable
fun AppHeader(mainViewModel: MainViewModel) {
    val isConnected by mainViewModel.socketConnected.collectAsState()

    Row {
        Text("Hockey Predictions")

        // Show indicator
        if (isConnected) {
            Icon(Icons.Default.Wifi, tint = Color.Green)
        } else {
            Icon(Icons.Default.WifiOff, tint = Color.Red)
        }
    }
}
```

### Example 4: Listen for User Joining Challenge

```kotlin
@Composable
fun ChallengeDetailsScreen(
    challengeId: String,
    socketEventListener: SocketEventListener
) {
    val members = remember { mutableStateListOf<String>() }

    // Listen for new members
    LaunchedEffect(challengeId) {
        socketEventListener.userJoinedChallenge.collect { event ->
            if (event.challengeId == challengeId) {
                // Add new member to list
                val userName = event.data?.optJSONObject("user")?.optString("name")
                userName?.let { members.add(it) }

                // Show toast
                showToast("${userName} joined!")
            }
        }
    }

    // Display members...
}
```

## 🔧 Configuration

### Change Server URL

Edit `SocketManager.kt`:

```kotlin
companion object {
    // For Android Emulator (localhost on your computer)
    private const val SERVER_URL = "http://10.0.2.2:3000"

    // For Physical Device (use your computer's local IP)
    // private const val SERVER_URL = "http://192.168.1.100:3000"

    // For Production
    // private const val SERVER_URL = "https://your-domain.com"
}
```

### Enable/Disable Auto-Connect

Edit `MainViewModel.kt`:

```kotlin
init {
    // Comment out to disable auto-connect
    // connectSocket()
    // socketEventListener.startListening()
}

// Then manually connect later:
fun onUserLoggedIn() {
    connectSocket()
    socketEventListener.startListening()
}
```

## 🧪 Testing

### 1. Check Connection Status

Add logging to your UI:

```kotlin
LaunchedEffect(Unit) {
    mainViewModel.socketConnected.collect { connected ->
        Log.d("Socket", "Connected: $connected")
    }
}
```

### 2. Test Event Reception

```kotlin
LaunchedEffect(Unit) {
    socketEventListener.systemAnnouncement.collect { event ->
        Log.d("Socket", "Announcement: ${event.message}")
    }
}
```

### 3. Trigger Test Event

From backend terminal:

```bash
curl -X POST http://localhost:3000/api/socket/broadcast \
  -H "Content-Type: application/json" \
  -d '{"event":"system_announcement","message":"Test from server!"}'
```

## 🐛 Troubleshooting

### Socket won't connect

- **Check SERVER_URL** - Use `10.0.2.2` for emulator, or your PC's IP for device
- **Check backend is running** - `http://localhost:3000` should respond
- **Check firewall** - Allow port 3000

### Events not received

- **Check authentication** - Must call `authenticateSocket()` after login
- **Check room** - Must call `joinChallengeRoom()` to get challenge events
- **Check listener** - Must call `startListening()` in init

### Connection drops

- Socket.IO auto-reconnects (configured in SocketManager)
- Check logs for reconnection attempts
- Re-authenticate after reconnect

## 📝 Next Steps

1. **Add to Hilt Module** (if needed) - SocketManager is already `@Singleton`
2. **Authenticate after login** - Call `mainViewModel.authenticateSocket(userId, email)`
3. **Join rooms in screens** - Use `joinChallengeRoom()` in relevant screens
4. **Handle events** - Collect flows in composables
5. **Show notifications** - Use Android notification system for background events

## 🎉 Summary

**Where socket connects:** `MainViewModel.init {}` block when app starts

**How to use:**

1. Socket automatically connects on app start
2. Authenticate after user logs in
3. Join rooms when viewing challenges
4. Collect event flows in your composables
5. Socket auto-disconnects when app closes

**Files to remember:**

- `SocketManager.kt` - Connection management
- `SocketEventListener.kt` - Event handling
- `MainViewModel.kt` - Lifecycle management
