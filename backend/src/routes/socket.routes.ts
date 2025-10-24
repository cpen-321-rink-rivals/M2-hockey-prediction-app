import { Router, Request, Response } from 'express';
import SocketEvents from '../socket.events';

const router = Router();

// Test endpoint to trigger socket events
router.post('/broadcast', (req: Request, res: Response) => {
  try {
    const { event, message, data, targetUserId, challengeId } = req.body;

    switch (event) {
      case 'system_announcement':
        SocketEvents.systemAnnouncement(message, data);
        break;

      case 'notification':
        if (targetUserId) {
          SocketEvents.sendNotification(targetUserId, { message, data });
        }
        break;

      case 'challenge_update':
        if (challengeId) {
          global.socketService.broadcastToChallenge(
            challengeId,
            'test_update',
            { message, data }
          );
        }
        break;

      default:
        global.socketService.broadcastToAll('test_message', { message, data });
    }

    res.json({
      success: true,
      message: 'Socket event broadcasted successfully',
      event,
      timestamp: new Date().toISOString(),
    });
  } catch (error) {
    console.error('Socket broadcast error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to broadcast socket event',
      error: error instanceof Error ? error.message : 'Unknown error',
    });
  }
});

// Get socket service status
router.get('/status', (req: Request, res: Response) => {
  const connectedUsers = global.socketService?.getConnectedUsersCount() || 0;

  res.json({
    success: true,
    socketService: {
      isActive: !!global.socketService,
      connectedUsers,
      timestamp: new Date().toISOString(),
    },
  });
});

export default router;
