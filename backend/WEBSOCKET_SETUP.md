# WebSocket Live Updates Setup

## 🚀 What's Been Added

Your TypeScript Node.js backend now has **WebSocket support** for real-time updates using Socket.IO!

## 📦 Components Added

### 1. `socket.service.ts` - Core WebSocket Service

- Handles client connections and disconnections
- Manages user authentication and room joining
- Provides methods for broadcasting messages

### 2. `socket.events.ts` - Event Utilities

- Pre-built functions for common events (challenges, games, friends)
- Easy-to-use methods for broadcasting updates

### 3. `socket.routes.ts` - Test API Endpoints

- `/api/socket/broadcast` - Trigger socket events via HTTP
- `/api/socket/status` - Check WebSocket service status

### 4. WebSocket Test Client

- Available at `http://localhost:3000/test`
- Interactive HTML page to test WebSocket functionality

## 🔧 How to Use

### Start the Server

```bash
cd backend
npm run build
npm start
```

### Test WebSocket Connection

1. Visit `http://localhost:3000/test` in your browser
2. Click "Connect" to establish WebSocket connection
3. Enter a User ID and click "Authenticate"
4. Test various features like joining challenge rooms

### Integration in Your Code

#### Emit Events from Controllers

```typescript
import SocketEvents from '../socket.events';

// In your challenge controller
SocketEvents.challengeCreated(challengeData);
SocketEvents.userJoinedChallenge(challengeId, userData, challengeData);
```

#### Broadcast Custom Events

```typescript
// Broadcast to all users
global.socketService.broadcastToAll('custom_event', {
  message: 'Hello everyone!',
});

// Send to specific user
global.socketService.sendToUser(userId, 'notification', { message: 'Hello!' });

// Broadcast to challenge participants
global.socketService.broadcastToChallenge(challengeId, 'update', {
  message: 'Challenge updated!',
});
```

## 📱 Frontend Integration (Android/React Native)

To connect from your frontend, you'll need to install a Socket.IO client:

```bash
# For React Native
npm install socket.io-client

# For Android (add to build.gradle)
implementation 'io.socket:socket.io-client:latest.release'
```

### Android Kotlin Example

```kotlin
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

class SocketManager {
    private lateinit var socket: Socket

    fun connect() {
        socket = IO.socket("http://your-server:3000")

        socket.on(Socket.EVENT_CONNECT) {
            // Authenticate user
            socket.emit("authenticate", JSONObject().apply {
                put("userId", "user-123")
                put("userEmail", "user@example.com")
            })
        }

        socket.on("challenge_created") { args ->
            // Handle challenge created event
            val data = args[0] as JSONObject
            // Update UI
        }

        socket.connect()
    }
}
```

## 🎯 Current Events Already Integrated

- ✅ `challenge_created` - When a new challenge is created
- ✅ `user_joined_challenge` - When someone joins a challenge
- Ready to add: `challenge_updated`, `user_left_challenge`, `game_score_update`

## 🔒 Security Notes

- Authentication is handled via the `authenticate` socket event
- Users are placed in personal rooms (`user:userId`) for targeted messages
- Challenge rooms (`challenge:challengeId`) for group communications
- Remember to configure CORS properly for production

## 🧪 Testing Commands

### Test via HTTP API

```bash
# System announcement
curl -X POST http://localhost:3000/api/socket/broadcast \
  -H "Content-Type: application/json" \
  -d '{"event": "system_announcement", "message": "Server maintenance in 5 minutes"}'

# User notification
curl -X POST http://localhost:3000/api/socket/broadcast \
  -H "Content-Type: application/json" \
  -d '{"event": "notification", "targetUserId": "user-123", "message": "You have a new challenge!"}'

# Check status
curl http://localhost:3000/api/socket/status
```

## 🚀 Next Steps

1. **Test the setup** using the web client at `/test`
2. **Integrate with your Android app** using Socket.IO client
3. **Add more events** to controllers where needed
4. **Configure production settings** (CORS, authentication, etc.)

Your WebSocket system is now ready for real-time hockey prediction updates! 🏒
