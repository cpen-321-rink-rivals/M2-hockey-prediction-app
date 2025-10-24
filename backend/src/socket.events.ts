// Socket event utilities for easy broadcasting
export class SocketEvents {
  // Challenge-related events
  static challengeCreated(challengeData: any) {
    if (global.socketService) {
      // Notify all invited users
      if (challengeData.invitedUserIds?.length > 0) {
        global.socketService.sendToUsers(
          challengeData.invitedUserIds,
          'challenge_invitation',
          {
            type: 'challenge_created',
            challenge: challengeData,
            message: `You've been invited to join "${challengeData.title}"`,
          }
        );
      }

      // Notify challenge owner
      global.socketService.sendToUser(
        challengeData.ownerId,
        'challenge_created',
        {
          type: 'challenge_created',
          challenge: challengeData,
          message: 'Your challenge has been created successfully!',
        }
      );
    }
  }

  static challengeUpdated(challengeId: string, challengeData: any) {
    if (global.socketService) {
      global.socketService.broadcastToChallenge(
        challengeId,
        'challenge_updated',
        {
          type: 'challenge_updated',
          challenge: challengeData,
          message: 'Challenge has been updated',
        }
      );
    }
  }

  static userJoinedChallenge(
    challengeId: string,
    userData: any,
    challengeData: any
  ) {
    if (global.socketService) {
      global.socketService.broadcastToChallenge(
        challengeId,
        'user_joined_challenge',
        {
          type: 'user_joined',
          user: userData,
          challenge: challengeData,
          message: `${userData.name || userData.email || 'Someone'} joined the challenge!`,
        }
      );
    }
  }

  static userLeftChallenge(
    challengeId: string,
    userData: any,
    challengeData: any
  ) {
    if (global.socketService) {
      global.socketService.broadcastToChallenge(
        challengeId,
        'user_left_challenge',
        {
          type: 'user_left',
          user: userData,
          challenge: challengeData,
          message: `${userData.name || userData.email || 'Someone'} left the challenge`,
        }
      );
    }
  }

  static challengeStatusChanged(
    challengeId: string,
    newStatus: string,
    challengeData: any
  ) {
    if (global.socketService) {
      global.socketService.broadcastToChallenge(
        challengeId,
        'challenge_status_changed',
        {
          type: 'status_changed',
          challengeId,
          newStatus,
          challenge: challengeData,
          message: `Challenge status changed to ${newStatus}`,
        }
      );
    }
  }

  // Game/NHL-related events
  static gameScoreUpdate(gameId: string, scoreData: any) {
    if (global.socketService) {
      // Broadcast to all users interested in this game
      global.socketService.broadcastToAll('game_score_update', {
        type: 'score_update',
        gameId,
        score: scoreData,
        message: 'Game score updated!',
      });
    }
  }

  static gameStatusUpdate(gameId: string, status: string, gameData: any) {
    if (global.socketService) {
      global.socketService.broadcastToAll('game_status_update', {
        type: 'game_status',
        gameId,
        status,
        game: gameData,
        message: `Game status: ${status}`,
      });
    }
  }

  // Friend-related events
  static friendRequestReceived(userId: string, friendRequestData: any) {
    if (global.socketService) {
      global.socketService.sendToUser(userId, 'friend_request_received', {
        type: 'friend_request',
        friendRequest: friendRequestData,
        message: `New friend request from ${friendRequestData.senderName || 'someone'}!`,
      });
    }
  }

  static friendRequestAccepted(userId: string, friendData: any) {
    if (global.socketService) {
      global.socketService.sendToUser(userId, 'friend_request_accepted', {
        type: 'friend_accepted',
        friend: friendData,
        message: `${friendData.name || 'Someone'} accepted your friend request!`,
      });
    }
  }

  // Generic notification
  static sendNotification(userId: string, notification: any) {
    if (global.socketService) {
      global.socketService.sendToUser(userId, 'notification', notification);
    }
  }

  // Broadcast system announcement
  static systemAnnouncement(message: string, data?: any) {
    if (global.socketService) {
      global.socketService.broadcastToAll('system_announcement', {
        type: 'system',
        message,
        data,
        timestamp: new Date().toISOString(),
      });
    }
  }
}

export default SocketEvents;
