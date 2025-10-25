# Quick Start Guide - Game Status Sync

## What Was Implemented

Automatic challenge status updates based on NHL game state:
- ✅ **NHL Service**: Fetches game data from NHL API
- ✅ **Game Status Sync Job**: Polls challenges every 60 seconds
- ✅ **Auto Status Updates**: ACTIVE → LIVE → FINISHED
- ✅ **Socket Events**: Real-time notifications to all members
- ✅ **Frontend Support**: Already integrated in your Android app

## How It Works

1. **Backend polls** every 60 seconds
2. **Checks challenges** with status PENDING, ACTIVE, or LIVE
3. **Fetches game state** from NHL API for each gameId
4. **Updates status** when game starts or ends
5. **Emits socket event** to notify connected clients
6. **Frontend auto-refreshes** when event received

## Testing

### Option 1: With Real NHL Games

1. **Start the backend**:
   ```bash
   cd backend
   npm run dev
   ```

2. **Find today's games**:
   - Visit: https://api-web.nhle.com/v1/schedule/now
   - Look for game IDs in the response (e.g., `2024020123`)
   - Note the `gameState` field: `FUT`, `LIVE`, `OFF`, etc.

3. **Create a challenge** via your Android app:
   - Use a real game ID from step 2
   - Choose a game that will start soon or is already live
   - Invite some users or join yourself

4. **Watch the logs**:
   ```
   🔄 Running game status sync...
   Checking 1 challenge(s) for game status updates
   🔴 Challenge abc123: Game 2024020123 is now LIVE (was active)
   ✅ Challenge abc123 status updated: active → live
   ```

5. **Check your Android app**:
   - The challenge status should automatically update
   - No need to refresh manually
   - Socket events trigger UI updates

### Option 2: Test the NHL Service Directly

```bash
cd backend
npx ts-node scripts/testGameStatusSync.ts
```

This will:
- Fetch a test game from the NHL API
- Display game state and metadata
- Show recommended polling intervals
- List today's games

### Option 3: Monitor Logs Only

```bash
cd backend
npm run dev | grep "game status"
```

This filters logs to show only game status sync activities.

## Expected Behavior

### When Game Starts
```
🔴 Challenge abc123: Game 2024020123 is now LIVE (was active)
Notifying 3 member(s) of status change
✅ Challenge abc123 status updated: active → live
```

### When Game Ends
```
🏁 Challenge abc123: Game 2024020123 is now FINISHED (was live)
Notifying 3 member(s) of status change
✅ Challenge abc123 status updated: live → finished
```

### Normal Polling (No Changes)
```
🔄 Running game status sync...
Checking 5 challenge(s) for game status updates
Challenge abc123: No status change needed (game: FUT, challenge: active)
✅ Game status sync completed
```

## Configuration

### Adjust Polling Interval

In `backend/src/index.ts`:
```typescript
// Default: 60 seconds
gameStatusSyncJob.start(60000);

// More frequent (30 seconds) - for live games
gameStatusSyncJob.start(30000);

// Less frequent (5 minutes) - for testing
gameStatusSyncJob.start(300000);
```

### Stop the Job

```typescript
gameStatusSyncJob.stop();
```

### Sync Specific Challenge

```typescript
await gameStatusSyncJob.syncChallenge('challenge-id-here');
```

## Troubleshooting

### No status updates happening

**Check 1**: Are there challenges in ACTIVE/LIVE status?
```bash
# In MongoDB or via API
db.challenges.find({ status: { $in: ['active', 'live'] } })
```

**Check 2**: Is the job running?
```bash
# Check logs for:
🏒 Game status sync job started
```

**Check 3**: Is the NHL API accessible?
```bash
curl https://api-web.nhle.com/v1/schedule/now
```

**Check 4**: Are gameIds valid?
- GameIds should be NHL game IDs (numeric, e.g., `2024020123`)
- Find valid IDs from the NHL API schedule endpoint

### Status stuck at LIVE after game ends

- Check if the game is actually finished in NHL API
- The job might need 1-2 minutes to detect the change
- NHL API might have delay in marking game as final

### Socket events not received on frontend

1. **Check socket connection**: Look for "authenticated" in Android logs
2. **Check event listener**: Verify `challengeStatusChanged` flow is collected
3. **Check logs**: Backend should show "Notifying X member(s)"

## Files Created/Modified

### New Files
- ✅ `backend/src/services/nhl.service.ts` - NHL API integration
- ✅ `backend/src/jobs/gameStatusSync.job.ts` - Status sync scheduler
- ✅ `backend/scripts/testGameStatusSync.ts` - Test script
- ✅ `backend/GAME_STATUS_SYNC.md` - Detailed documentation

### Modified Files
- ✅ `backend/src/index.ts` - Start job on server boot
- ✅ `backend/src/socket.events.ts` - Enhanced status change event
- ✅ `backend/package.json` - Added axios dependency

### Frontend (Already Integrated)
- ✅ `SocketEventListener.kt` - Listens for `challenge_status_changed`
- ✅ `ChallengesScreen.kt` - Auto-refreshes on status change
- ✅ `EditChallengeScreen.kt` - Updates challenge details

## What's Next?

Your app now automatically updates challenge statuses! 🎉

### Recommended Next Steps

1. **Test with real games** - Find games starting soon
2. **Monitor logs** - Watch status transitions
3. **Check frontend** - Verify UI updates automatically
4. **Adjust polling** - Fine-tune based on your needs

### Future Enhancements (Optional)

- Add game score updates via socket events
- Show "Game starting in X minutes" countdown
- Add admin panel to manually override status
- Implement game postponement detection
- Add retry logic for failed API calls

## Need Help?

Check the detailed documentation in `GAME_STATUS_SYNC.md` for:
- Architecture details
- API endpoints
- Polling strategies
- Error handling
- Performance considerations
