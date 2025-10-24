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
      // Broadcast to challenge room (for users currently viewing the challenge)
      global.socketService.broadcastToChallenge(
        challengeId,
        'challenge_updated',
        {
          type: 'challenge_updated',
          challengeId: challengeId,
          challenge: challengeData,
          message: 'Challenge has been updated',
        }
      );

      // Also notify all members individually (for challenges list view)
      if (challengeData.memberIds?.length > 0) {
        global.socketService.sendToUsers(
          challengeData.memberIds,
          'challenge_updated',
          {
            type: 'challenge_updated',
            challengeId: challengeId,
            challenge: challengeData,
            message: 'Challenge has been updated',
          }
        );
      }

      // Notify invited users too
      if (challengeData.invitedUserIds?.length > 0) {
        global.socketService.sendToUsers(
          challengeData.invitedUserIds,
          'challenge_updated',
          {
            type: 'challenge_updated',
            challengeId: challengeId,
            challenge: challengeData,
            message: 'Challenge has been updated',
          }
        );
      }
    }
  }

  // challenge deleted
  static challengeDeleted(challengeId: string, challengeData: any) {
    if (global.socketService) {
      // Notify all members
      if (challengeData.memberIds?.length > 0) {
        global.socketService.sendToUsers(
          challengeData.memberIds,
          'challenge_deleted',
          {
            type: 'challenge_deleted',
            challengeId: challengeId,
            challenge: challengeData,
            message: `The challenge "${challengeData.title}" has been deleted.`,
          }
        );
      }

      // Notify invited users
      if (challengeData.invitedUserIds?.length > 0) {
        global.socketService.sendToUsers(
          challengeData.invitedUserIds,
          'challenge_deleted',
          {
            type: 'challenge_deleted',
            challengeId: challengeId,
            challenge: challengeData,
            message: `The challenge "${challengeData.title}" has been deleted.`,
          }
        );
      }
    }
  }

  static userJoinedChallenge(
    challengeId: string,
    userData: any,
    challengeData: any
  ) {
    if (global.socketService) {
      // Broadcast to challenge room
      global.socketService.broadcastToChallenge(
        challengeId,
        'user_joined_challenge',
        {
          type: 'user_joined',
          challengeId: challengeId,
          user: userData,
          challenge: challengeData,
          message: `${userData.name || userData.email || 'Someone'} joined the challenge!`,
        }
      );

      // Notify all members individually
      if (challengeData.memberIds?.length > 0) {
        global.socketService.sendToUsers(
          challengeData.memberIds,
          'user_joined_challenge',
          {
            type: 'user_joined',
            challengeId: challengeId,
            user: userData,
            challenge: challengeData,
            message: `${userData.name || userData.email || 'Someone'} joined the challenge!`,
          }
        );
      }
    }
  }

  static userLeftChallenge(
    challengeId: string,
    userData: any,
    challengeData: any
  ) {
    if (global.socketService) {
      // Broadcast to challenge room
      global.socketService.broadcastToChallenge(
        challengeId,
        'user_left_challenge',
        {
          type: 'user_left',
          challengeId: challengeId,
          user: userData,
          challenge: challengeData,
          message: `${userData.name || userData.email || 'Someone'} left the challenge`,
        }
      );

      // Notify all members individually
      if (challengeData.memberIds?.length > 0) {
        global.socketService.sendToUsers(
          challengeData.memberIds,
          'user_left_challenge',
          {
            type: 'user_left',
            challengeId: challengeId,
            user: userData,
            challenge: challengeData,
            message: `${userData.name || userData.email || 'Someone'} left the challenge`,
          }
        );
      }
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
