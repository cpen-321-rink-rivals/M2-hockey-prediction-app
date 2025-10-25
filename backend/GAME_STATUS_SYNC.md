# Game Status Sync Feature

## Overview
Automatically updates challenge statuses based on NHL game states:
- **PENDING/ACTIVE → LIVE**: When the game starts
- **LIVE → FINISHED**: When the game ends

## Architecture

### Components

1. **NHL Service** (`services/nhl.service.ts`)
   - Fetches game data from NHL API
   - Caches responses (30s TTL)
   - Parses game states: FUT, LIVE, CRIT, OFF, FINAL
   - Provides adaptive polling intervals

2. **Game Status Sync Job** (`jobs/gameStatusSync.job.ts`)
   - Polls challenges every 60 seconds
   - Checks PENDING, ACTIVE, and LIVE challenges
   - Updates status based on game state
   - Emits socket events for real-time updates

3. **Socket Events** (`socket.events.ts`)
   - `challenge_status_changed` event
   - Notifies challenge room and all members individually

## NHL API Integration

### Endpoints Used
- Primary: `https://api-web.nhle.com/v1/schedule/now`
- Fallback: `https://api-web.nhle.com/v1/gamecenter/{gameId}/landing`

### Game States
- `FUT` / `SCHEDULED`: Game scheduled (future)
- `LIVE`: Game in progress
- `CRIT`: Critical/close game (late stages)
- `OFF` / `FINAL`: Game finished
- `PRE`: Pre-game warmups

## Polling Strategy

The system uses adaptive polling based on game timing:

| Time Until Start | Polling Interval |
|-----------------|------------------|
| > 1 hour        | 10 minutes       |
| 10 min - 1 hour | 5 minutes        |
| < 10 minutes    | 1 minute         |
| During game     | 30 seconds       |
| After game      | 5 minutes        |

## Configuration

### Starting the Job
The job starts automatically when the server boots:

```typescript
// In src/index.ts
gameStatusSyncJob.start(60000); // 60 seconds interval
```

### Stopping the Job
```typescript
gameStatusSyncJob.stop();
```

### Manual Sync
Force sync a specific challenge:
```typescript
await gameStatusSyncJob.syncChallenge(challengeId);
```

## Data Flow

1. **Job polls database** → Finds challenges in PENDING/ACTIVE/LIVE states
2. **Fetch game status** → NHL Service calls API for each unique gameId
3. **Compare states** → Determines if status change is needed
4. **Update database** → Updates challenge.status if changed
5. **Emit socket event** → Notifies all connected clients
6. **Frontend updates** → UI automatically refreshes

## Status Transitions

### Valid Transitions
- PENDING → ACTIVE (when all invites resolved)
- PENDING → LIVE (game starts before invites resolved)
- ACTIVE → LIVE (game starts)
- LIVE → FINISHED (game ends)

### Invalid Transitions (Prevented)
- FINISHED → LIVE
- LIVE → ACTIVE
- CANCELLED → any other state

## Frontend Integration

The Android app already listens for `challenge_status_changed` events:

```kotlin
// In SocketEventListener.kt
val challengeStatusChanged: SharedFlow<ChallengeStatusChangedEvent>

// In ChallengesScreen.kt
LaunchedEffect(socketEventListener) {
    socketEventListener.challengeStatusChanged.collect { event ->
        // Refresh challenges list
        viewModel.loadChallenges()
    }
}
```

## Error Handling

- **API Failures**: Logged, challenge skipped, retried on next poll
- **Network Timeouts**: 10-second timeout, fails gracefully
- **Invalid Game IDs**: Logged warning, challenge skipped
- **Rate Limiting**: 30-second cache reduces API calls

## Logging

All status changes are logged:
```
🔴 Challenge abc123: Game 2024020123 is now LIVE (was active)
🏁 Challenge abc123: Game 2024020123 is now FINISHED (was live)
✅ Challenge abc123 status updated: active → live
```

## Testing

### Manual Testing
1. Create a challenge with a real NHL game ID
2. Wait for game to start (or use a game in progress)
3. Check logs for status updates
4. Verify socket events are emitted
5. Confirm frontend UI updates

### Test Script
```bash
# Start the backend
npm run dev

# Watch logs for status updates
# Create challenges via API or frontend
# Monitor socket events in browser console
```

## Performance Considerations

- **Caching**: 30-second cache reduces API load
- **Batch Processing**: All challenges checked in parallel
- **Efficient Queries**: Only checks non-terminal statuses
- **Error Isolation**: One failed challenge doesn't affect others

## Future Enhancements

- [ ] Add game score updates via socket events
- [ ] Implement exponential backoff for API failures
- [ ] Add webhook support for instant NHL updates
- [ ] Store game end time in challenge model
- [ ] Add manual status override for admins
- [ ] Implement game postponement handling
