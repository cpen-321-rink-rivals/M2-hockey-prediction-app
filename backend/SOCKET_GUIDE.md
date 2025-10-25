# WebSocket Implementation Guide - Step by Step

## Overview

Your hockey prediction app uses **Socket.IO** for real-time bidirectional communication between the backend (Node.js/TypeScript) and clients (Android app or web).

---

## Architecture Overview

```
┌─────────────────┐         WebSocket          ┌──────────────────┐
│  Client (App)   │ ←─────────────────────────→ │  Server (Node)   │
│                 │                              │                  │
│  - Connect      │    1. Connection Event       │  - Socket.IO     │
│  - Authenticate │    2. Authentication         │  - Room Manager  │
│  - Join Rooms   │    3. Join challenge:xxx     │  - Broadcaster   │
│  - Listen       │    4. Receive events         │                  │
└─────────────────┘                              └──────────────────┘
        ↑                                                  ↓
        │                                                  │
        └──────────────── Real-time Events ───────────────┘
           (challenge updates, notifications, etc.)
```

---

## Step 1: Server Initialization

### File: `src/index.ts`

```typescript
// 1. Create HTTP server (not just Express app)
const app = express();
const server = createServer(app); // ← Wraps Express in HTTP server

// 2. Initialize Socket.IO service
const socketService = new SocketService(server); // ← Pass HTTP server

// 3. Make it globally available
global.socketService = socketService;

// 4. Start listening
server.listen(PORT, () => {
  console.log(`🚀 Server running on port ${PORT}`);
  console.log(`🔗 WebSocket server ready`);
});
```

**What happens:**

- Express app is wrapped in an HTTP server
- Socket.IO attaches to this HTTP server
- Both HTTP requests and WebSocket connections use the same port (3000)
- `global.socketService` lets other modules emit events

---

## Step 2: Socket Service Setup

### File: `src/socket.service.ts`

```typescript
class SocketService {
  private io: SocketIOServer;
  private connectedUsers: Map<string, AuthenticatedSocket>;

  constructor(server: HttpServer) {
    // 1. Create Socket.IO server
    this.io = new SocketIOServer(server, {
      cors: {
        origin: '*', // Allow all origins (configure for production!)
        methods: ['GET', 'POST'],
      },
    });

    // 2. Set up event handlers
    this.setupSocketHandlers();
  }
}
```

**Key components:**

- `io`: The Socket.IO server instance
- `connectedUsers`: Map of userId → socket connection
- `cors`: Allows connections from any origin (important for mobile apps)

---

## Step 3: Client Connection Flow

### When a client connects:

```typescript
this.io.on('connection', (socket: AuthenticatedSocket) => {
  console.log(`🔗 Socket connected: ${socket.id}`);

  // At this point:
  // - Client has a unique socket.id
  // - Connection is established
  // - But we don't know WHO this user is yet
});
```

**What's a socket.id?**

- Auto-generated unique ID for each connection
- Example: `"Gx3k9Kj2mP-_AAAB"`
- Changes every time the client reconnects

---

## Step 4: User Authentication

### Client sends authentication:

```typescript
socket.on('authenticate', (data: { userId: string; userEmail?: string }) => {
  // 1. Store user info on the socket
  socket.userId = data.userId;
  socket.userEmail = data.userEmail;

  // 2. Track this user
  this.connectedUsers.set(data.userId, socket);

  // 3. Join personal room
  socket.join(`user:${data.userId}`);

  // 4. Confirm authentication
  socket.emit('authenticated', { userId: data.userId });
});
```

**What are rooms?**
Rooms are like "channels" that sockets can join. Think of them as:

- `user:123` → Personal room for user 123
- `challenge:abc` → Room for everyone in challenge "abc"

**Why rooms?**
They let you target specific groups:

- Send to one user: emit to `user:123`
- Send to all in a challenge: emit to `challenge:abc`
- Send to everyone: broadcast to all

---

## Step 5: Joining Challenge Rooms

```typescript
socket.on('join_challenge', (challengeId: string) => {
  socket.join(`challenge:${challengeId}`);
  console.log(`🏒 User ${socket.userId} joined challenge room: ${challengeId}`);
});

socket.on('leave_challenge', (challengeId: string) => {
  socket.leave(`challenge:${challengeId}`);
  console.log(`🚪 User ${socket.userId} left challenge room: ${challengeId}`);
});
```

**Use case:**

- User opens "Challenge Details" screen → join that challenge's room
- User leaves the screen → leave the room
- Now they only get updates for challenges they're viewing

---

## Step 6: Broadcasting Events

### From Controller (HTTP endpoint):

```typescript
// File: src/controllers/challenges.controller.ts

async create(req: Request, res: Response) {
  const challenge = await challengeModel.create(req.body, req.user.id);

  // ✨ Emit socket event after creating challenge
  SocketEvents.challengeCreated(challenge);

  res.status(201).json({ success: true, data: challenge });
}
```

### Socket Event Helper:

```typescript
// File: src/socket.events.ts

static challengeCreated(challengeData: any) {
  // 1. Notify invited users
  global.socketService.sendToUsers(
    challengeData.invitedUserIds,
    'challenge_invitation',
    { challenge: challengeData, message: "You've been invited!" }
  );

  // 2. Notify owner
  global.socketService.sendToUser(
    challengeData.ownerId,
    'challenge_created',
    { challenge: challengeData, message: 'Challenge created!' }
  );
}
```

---

## Step 7: Broadcasting Methods

### Method 1: Send to specific user

```typescript
public sendToUser(userId: string, event: string, data: any) {
  this.io.to(`user:${userId}`).emit(event, data);
}
```

**Example:** Notify user "123" about a friend request

```typescript
socketService.sendToUser('123', 'friend_request', { from: 'John' });
```

---

### Method 2: Send to multiple users

```typescript
public sendToUsers(userIds: string[], event: string, data: any) {
  userIds.forEach(userId => {
    this.sendToUser(userId, event, data);
  });
}
```

**Example:** Notify all invited users about a challenge

```typescript
socketService.sendToUsers(['123', '456', '789'], 'challenge_invitation', {...});
```

---

### Method 3: Send to challenge room

```typescript
public broadcastToChallenge(challengeId: string, event: string, data: any) {
  this.io.to(`challenge:${challengeId}`).emit(event, data);
}
```

**Example:** Notify everyone in a challenge that someone joined

```typescript
socketService.broadcastToChallenge('abc', 'user_joined', { user: {...} });
```

---

### Method 4: Broadcast to everyone

```typescript
public broadcastToAll(event: string, data: any) {
  this.io.emit(event, data);
}
```

**Example:** System maintenance announcement

```typescript
socketService.broadcastToAll('system_announcement', {
  message: 'Maintenance in 5 min',
});
```

---

## Step 8: Client Receives Events

### Android App (Kotlin):

```kotlin
// 1. Connect to server
val socket = IO.socket("http://localhost:3000")
socket.connect()

// 2. Authenticate
socket.emit("authenticate", JSONObject().apply {
    put("userId", currentUserId)
    put("userEmail", currentUserEmail)
})

// 3. Listen for events
socket.on("challenge_invitation") { args ->
    val data = args[0] as JSONObject
    // Show notification: "You've been invited to a challenge!"
    showNotification(data.getString("message"))
}

socket.on("challenge_updated") { args ->
    val data = args[0] as JSONObject
    // Refresh challenge screen
    refreshChallengeData()
}

// 4. Join challenge room when viewing details
socket.emit("join_challenge", challengeId)

// 5. Leave room when leaving screen
socket.emit("leave_challenge", challengeId)
```

---

## Step 9: Disconnect Handling

```typescript
socket.on('disconnect', () => {
  if (socket.userId) {
    // Remove from tracking
    this.connectedUsers.delete(socket.userId);
    console.log(`👋 User ${socket.userId} disconnected`);
  }

  // Socket.IO automatically removes from all rooms
});
```

**What happens on disconnect:**

- User entry removed from `connectedUsers` map
- Socket automatically leaves all rooms
- Connection can be re-established (new socket.id)

---

## Complete Flow Example: User Joins Challenge

### 1. **HTTP Request (REST)**

```http
POST /api/challenges/abc/join
Authorization: Bearer <token>
Body: { ticketId: "ticket123" }
```

### 2. **Controller Handles Request**

```typescript
async joinChallenge(req: Request, res: Response) {
  const challenge = await challengeModel.joinChallenge(
    req.params.id,
    req.user.id,
    req.body.ticketId
  );

  // ✨ Emit socket event
  SocketEvents.userJoinedChallenge(req.params.id, req.user, challenge);

  res.json({ success: true, data: challenge });
}
```

### 3. **Socket Event Emitted**

```typescript
static userJoinedChallenge(challengeId: string, userData: any, challengeData: any) {
  global.socketService.broadcastToChallenge(
    challengeId,
    'user_joined_challenge',
    {
      type: 'user_joined',
      user: userData,
      challenge: challengeData,
      message: `${userData.name} joined the challenge!`
    }
  );
}
```

### 4. **All Clients in Room Receive Event**

Everyone who ran `socket.emit('join_challenge', 'abc')` receives:

```javascript
{
  type: 'user_joined',
  user: { name: 'John', ... },
  challenge: { id: 'abc', ... },
  message: 'John joined the challenge!'
}
```

### 5. **Client UI Updates**

```kotlin
socket.on("user_joined_challenge") { args ->
    val data = args[0] as JSONObject
    val userName = data.getJSONObject("user").getString("name")

    // Update member list
    addMemberToList(userName)

    // Show toast
    Toast.makeText(context, "$userName joined!", Toast.LENGTH_SHORT).show()
}
```

---

## Key Concepts Summary

### 1. **Socket vs Connection**

- **Socket**: Individual connection (has a unique `socket.id`)
- **User**: Can have multiple sockets (phone + tablet)
- Track by `userId` not `socket.id`

### 2. **Rooms**

- Logical groups of sockets
- Socket can be in multiple rooms
- Emit to room → all sockets in that room receive

### 3. **Events**

- Server can emit to client
- Client can emit to server
- Event names are strings (e.g., 'challenge_created')
- Data can be any JSON-serializable object

### 4. **Bi-directional**

- Client → Server: `socket.emit('event', data)`
- Server → Client: `socket.emit('event', data)`
- Server → Room: `io.to('room').emit('event', data)`
- Server → All: `io.emit('event', data)`

---

## Testing Your WebSocket

### 1. **Start Server**

```bash
cd backend
npm run dev
```

### 2. **Open Test Client**

```
http://localhost:3000/test
```

### 3. **Test Flow**

1. Click "Connect" → See connection log
2. Click "Authenticate" → Get confirmed
3. Enter challenge ID → Click "Join Challenge"
4. In terminal, trigger event:
   ```bash
   curl -X POST http://localhost:3000/api/socket/broadcast \
     -H "Content-Type: application/json" \
     -d '{"event":"challenge_update","challengeId":"challenge-123","message":"Test update"}'
   ```
5. See message appear in test client!

---

## Common Use Cases

### 1. **Real-time Challenge Updates**

```typescript
// When challenge status changes
SocketEvents.challengeStatusChanged(challengeId, 'live', challengeData);

// All users in that challenge's room get notified
```

### 2. **Friend Requests**

```typescript
// When user A sends request to user B
SocketEvents.friendRequestReceived(userB_id, { from: userA_name });

// User B's app shows notification immediately
```

### 3. **Game Score Updates**

```typescript
// When NHL game score updates
SocketEvents.gameScoreUpdate(gameId, { home: 3, away: 2 });

// All users watching that game get live updates
```

### 4. **Challenge Invitations**

```typescript
// When user creates challenge and invites friends
SocketEvents.challengeCreated(challenge);

// All invited friends get notification instantly
```

---

## Debugging Tips

### 1. **Server Logs**

```typescript
console.log(`🔗 Socket connected: ${socket.id}`);
console.log(`👤 User authenticated: ${userId}`);
console.log(`🏒 User joined challenge room: ${challengeId}`);
```

### 2. **Check Connected Users**

```bash
curl http://localhost:3000/api/socket/status
```

Returns: `{ connectedUsers: 5, isActive: true }`

### 3. **Client-side Logging**

```kotlin
socket.on(Socket.EVENT_CONNECT) {
    Log.d("Socket", "Connected!")
}
socket.on(Socket.EVENT_DISCONNECT) {
    Log.d("Socket", "Disconnected!")
}
socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
    Log.e("Socket", "Error: ${args[0]}")
}
```

---

## Security Considerations

### 1. **Authentication**

Currently: User sends their `userId` (trust-based)

**Better approach:**

```typescript
socket.on('authenticate', async (data: { token: string }) => {
  // Verify JWT token
  const decoded = jwt.verify(data.token, SECRET);
  socket.userId = decoded.userId;
  // Now you know this is a real, authenticated user
});
```

### 2. **Authorization**

```typescript
socket.on('join_challenge', async (challengeId: string) => {
  // Check if user is member of this challenge
  const isMember = await checkChallengeMembership(socket.userId, challengeId);
  if (isMember) {
    socket.join(`challenge:${challengeId}`);
  } else {
    socket.emit('error', { message: 'Not authorized' });
  }
});
```

### 3. **Rate Limiting**

Prevent spam:

```typescript
const messageCount = new Map<string, number>();

socket.on('message', data => {
  const count = messageCount.get(socket.userId) || 0;
  if (count > 100) {
    return socket.emit('error', { message: 'Rate limit exceeded' });
  }
  messageCount.set(socket.userId, count + 1);
});
```

---

## Performance Tips

### 1. **Clean up rooms**

```typescript
// Leave challenge room when done viewing
socket.emit('leave_challenge', challengeId);
```

### 2. **Batch updates**

Instead of emitting 100 times:

```typescript
const updates = [];
// collect updates...
socket.emit('batch_update', updates);
```

### 3. **Use acknowledgements**

```typescript
// Client
socket.emit('join_challenge', challengeId, response => {
  if (response.success) {
    console.log('Joined successfully');
  }
});

// Server
socket.on('join_challenge', (challengeId, callback) => {
  socket.join(`challenge:${challengeId}`);
  callback({ success: true });
});
```

---

## What's Next?

1. **Implement in Android app**: Add Socket.IO client library
2. **Add authentication**: Use JWT tokens for socket auth
3. **Handle reconnection**: Auto-reconnect with exponential backoff
4. **Add typing indicators**: Show "User is typing..." in chat
5. **Presence system**: Show who's online/offline
6. **Message read receipts**: Track message delivery

---

## Quick Reference

| Operation              | Code                                                           |
| ---------------------- | -------------------------------------------------------------- |
| Emit to one user       | `socketService.sendToUser(userId, event, data)`                |
| Emit to multiple users | `socketService.sendToUsers([...ids], event, data)`             |
| Emit to challenge room | `socketService.broadcastToChallenge(challengeId, event, data)` |
| Emit to everyone       | `socketService.broadcastToAll(event, data)`                    |
| Check if online        | `socketService.isUserOnline(userId)`                           |
| Get connected count    | `socketService.getConnectedUsersCount()`                       |

---

## Questions?

- **Q: Can one user have multiple connections?**  
  A: Yes! If they're on phone and tablet, each has its own socket.

- **Q: What if user disconnects?**  
  A: Socket.IO handles reconnection automatically. You just need to re-authenticate.

- **Q: How do I know if a message was received?**  
  A: Use acknowledgements (callbacks) or implement read receipts.

- **Q: Can I send binary data?**  
  A: Yes! Socket.IO supports binary (images, files, etc.)

- **Q: What's the difference between HTTP and WebSocket?**  
  A: HTTP = Request/Response (client asks, server responds)  
   WebSocket = Bidirectional (both can send anytime)

---

**You now have real-time communication in your hockey prediction app! 🏒⚡**
