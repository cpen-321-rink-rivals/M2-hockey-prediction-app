import { challengeModel } from '../models/challenges.model';
import {
  ChallengeStatus,
  ChallengeStatusType,
} from '../types/challenges.types';
import { nhlService } from '../services/nhl.service';
import SocketEvents from '../socket.events';
import logger from '../logger.util';

export class GameStatusSyncJob {
  private intervalId: NodeJS.Timeout | null = null;
  private isRunning: boolean = false;
  private readonly DEFAULT_INTERVAL = 60000; // 1 minute default

  /**
   * Start the game status synchronization job
   * @param interval - Polling interval in milliseconds (default: 60000ms = 1 minute)
   */
  start(interval: number = this.DEFAULT_INTERVAL) {
    if (this.isRunning) {
      logger.warn('Game status sync job is already running');
      return;
    }

    logger.info(
      `🏒 Starting game status sync job (interval: ${interval / 1000}s)`
    );
    this.isRunning = true;

    // Run immediately on start
    this.syncGameStatuses().catch(error => {
      logger.error('Error in initial game status sync:', error);
    });

    // Then run on interval
    this.intervalId = setInterval(() => {
      this.syncGameStatuses().catch(error => {
        logger.error('Error in game status sync interval:', error);
      });
    }, interval);
  }

  /**
   * Stop the game status synchronization job
   */
  stop() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
      this.isRunning = false;
      logger.info('🛑 Game status sync job stopped');
    }
  }

  /**
   * Main synchronization logic
   */
  private async syncGameStatuses() {
    try {
      logger.debug('🔄 Running game status sync...');

      // Get all challenges that need status checking
      // Only check PENDING, ACTIVE, and LIVE challenges
      const challenges = await challengeModel.findAll(1, 1000); // Get up to 1000 challenges

      const challengesToCheck = challenges.challenges.filter(challenge => {
        const statusesToCheck: ChallengeStatusType[] = [
          ChallengeStatus.PENDING,
          ChallengeStatus.ACTIVE,
          ChallengeStatus.LIVE,
        ];
        return statusesToCheck.includes(
          challenge.status as ChallengeStatusType
        );
      });

      if (challengesToCheck.length === 0) {
        logger.debug('No challenges require status checking');
        return;
      }

      logger.info(
        `Checking ${challengesToCheck.length} challenge(s) for game status updates`
      );

      // Process each challenge
      const updatePromises = challengesToCheck.map(challenge =>
        this.updateChallengeStatus(challenge)
      );

      await Promise.allSettled(updatePromises);

      logger.debug('✅ Game status sync completed');
    } catch (error) {
      logger.error('Error syncing game statuses:', error);
    }
  }

  /**
   * Update status for a single challenge based on game state
   */
  private async updateChallengeStatus(challenge: any) {
    try {
      const gameId = challenge.gameId;
      if (!gameId) {
        logger.warn(`Challenge ${challenge.id} has no gameId`);
        return;
      }

      // Fetch game status from NHL API
      const gameStatus = await nhlService.getGameStatus(gameId);

      if (!gameStatus) {
        logger.warn(
          `Could not fetch game status for challenge ${challenge.id} (game ${gameId})`
        );
        return;
      }

      // Determine new status based on game state
      let newStatus: ChallengeStatusType | null = null;

      if (gameStatus.isLive && challenge.status !== ChallengeStatus.LIVE) {
        // Game has started - change to LIVE
        newStatus = ChallengeStatus.LIVE;
        logger.info(
          `🔴 Challenge ${challenge.id}: Game ${gameId} is now LIVE (was ${challenge.status})`
        );
      } else if (
        gameStatus.isFinished &&
        challenge.status !== ChallengeStatus.FINISHED
      ) {
        // Game has ended - change to FINISHED
        newStatus = ChallengeStatus.FINISHED;
        logger.info(
          `🏁 Challenge ${challenge.id}: Game ${gameId} is now FINISHED (was ${challenge.status})`
        );
      }

      // Update challenge if status changed
      if (newStatus) {
        const updatedChallenge = await challengeModel.update(challenge.id, {
          status: newStatus,
        });

        if (updatedChallenge) {
          // Emit socket event to notify all members
          SocketEvents.challengeStatusChanged(
            challenge.id,
            newStatus,
            updatedChallenge
          );

          // Also notify members individually
          if (updatedChallenge.memberIds?.length > 0) {
            logger.info(
              `Notifying ${updatedChallenge.memberIds.length} member(s) of status change`
            );
          }

          logger.info(
            `✅ Challenge ${challenge.id} status updated: ${challenge.status} → ${newStatus}`
          );
        }
      } else {
        logger.debug(
          `Challenge ${challenge.id}: No status change needed (game: ${gameStatus.gameState}, challenge: ${challenge.status})`
        );
      }
    } catch (error) {
      logger.error(
        `Error updating challenge ${challenge.id} status:`,
        error instanceof Error ? error.message : error
      );
    }
  }

  /**
   * Force sync for a specific challenge (useful for manual triggers)
   */
  async syncChallenge(challengeId: string) {
    try {
      const challenge = await challengeModel.findById(challengeId);
      if (!challenge) {
        logger.warn(`Challenge ${challengeId} not found`);
        return;
      }

      await this.updateChallengeStatus(challenge);
    } catch (error) {
      logger.error(`Error syncing challenge ${challengeId}:`, error);
      throw error;
    }
  }

  /**
   * Get job status
   */
  getStatus() {
    return {
      isRunning: this.isRunning,
      interval: this.intervalId ? this.DEFAULT_INTERVAL : null,
    };
  }
}

// Export singleton instance
export const gameStatusSyncJob = new GameStatusSyncJob();
export default gameStatusSyncJob;
