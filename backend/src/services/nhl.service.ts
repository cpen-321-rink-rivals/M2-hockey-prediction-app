import axios from 'axios';
import logger from '../logger.util';

// NHL API base URL - using the new NHL API (as of 2024+)
const NHL_API_BASE = 'https://api-web.nhle.com/v1';

export interface GameStatus {
  gameId: string;
  gameState: string; // "FUT" (future), "LIVE", "CRIT" (critical/close), "OFF" (official/final), etc.
  gameScheduleState: string; // "OK", "POSTPONED", etc.
  startTimeUTC: string;
  detailedState?: string;
  isLive: boolean;
  isFinished: boolean;
  isScheduled: boolean;
}

export class NHLService {
  private cache: Map<
    string,
    { data: GameStatus; timestamp: number }
  > = new Map();
  private readonly CACHE_TTL = 30000; // 30 seconds cache

  /**
   * Fetch game status from NHL API
   * @param gameId - The NHL game ID (gamePk)
   * @returns GameStatus object with current game state
   */
  async getGameStatus(gameId: string): Promise<GameStatus | null> {
    try {
      // Check cache first
      const cached = this.cache.get(gameId);
      if (cached && Date.now() - cached.timestamp < this.CACHE_TTL) {
        logger.debug(`Using cached game status for ${gameId}`);
        return cached.data;
      }

      // Fetch from NHL API
      // The new NHL API uses schedule endpoints that include game state
      const url = `${NHL_API_BASE}/schedule/now`;
      logger.debug(`Fetching NHL schedule from: ${url}`);

      const response = await axios.get(url, {
        timeout: 10000,
        headers: {
          'User-Agent': 'Hockey-Prediction-App/1.0',
        },
      });

      // Find the specific game in the schedule
      let gameData: any = null;
      if (response.data.gameWeek) {
        for (const day of response.data.gameWeek) {
          if (day.games) {
            gameData = day.games.find(
              (g: any) => g.id.toString() === gameId.toString()
            );
            if (gameData) break;
          }
        }
      }

      if (!gameData) {
        // Game not found in current schedule, try fetching specific game
        logger.warn(
          `Game ${gameId} not found in current schedule, trying specific endpoint`
        );
        return await this.getGameStatusDirect(gameId);
      }

      const status: GameStatus = {
        gameId: gameData.id.toString(),
        gameState: gameData.gameState || 'FUT',
        gameScheduleState: gameData.gameScheduleState || 'OK',
        startTimeUTC: gameData.startTimeUTC,
        detailedState: gameData.gameScheduleState,
        isLive: this.isGameLive(gameData.gameState),
        isFinished: this.isGameFinished(gameData.gameState),
        isScheduled: this.isGameScheduled(gameData.gameState),
      };

      // Cache the result
      this.cache.set(gameId, { data: status, timestamp: Date.now() });

      logger.info(
        `Game ${gameId} status: ${status.gameState} (Live: ${status.isLive}, Finished: ${status.isFinished})`
      );

      return status;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        logger.error(
          `Error fetching game ${gameId} from NHL API: ${error.message}`
        );
        if (error.response) {
          logger.error(`Response status: ${error.response.status}`);
          logger.error(`Response data: ${JSON.stringify(error.response.data)}`);
        }
      } else {
        logger.error(`Unexpected error fetching game ${gameId}:`, error);
      }
      return null;
    }
  }

  /**
   * Fetch game status directly from game-specific endpoint
   * @param gameId - The NHL game ID
   */
  private async getGameStatusDirect(gameId: string): Promise<GameStatus | null> {
    try {
      // Try the game-specific landing endpoint
      const url = `${NHL_API_BASE}/gamecenter/${gameId}/landing`;
      logger.debug(`Fetching game directly from: ${url}`);

      const response = await axios.get(url, {
        timeout: 10000,
        headers: {
          'User-Agent': 'Hockey-Prediction-App/1.0',
        },
      });

      if (!response.data) {
        logger.warn(`No data returned for game ${gameId}`);
        return null;
      }

      const gameState = response.data.gameState || 'FUT';
      const status: GameStatus = {
        gameId: gameId,
        gameState: gameState,
        gameScheduleState: response.data.gameScheduleState || 'OK',
        startTimeUTC: response.data.startTimeUTC || new Date().toISOString(),
        detailedState: response.data.gameScheduleState,
        isLive: this.isGameLive(gameState),
        isFinished: this.isGameFinished(gameState),
        isScheduled: this.isGameScheduled(gameState),
      };

      // Cache the result
      this.cache.set(gameId, { data: status, timestamp: Date.now() });

      return status;
    } catch (error) {
      logger.error(`Error fetching game ${gameId} directly:`, error);
      return null;
    }
  }

  /**
   * Check if game is currently live/in progress
   */
  private isGameLive(gameState: string): boolean {
    const liveStates = ['LIVE', 'CRIT', 'PRE']; // PRE = pregame, might want to treat as live
    return liveStates.includes(gameState.toUpperCase());
  }

  /**
   * Check if game is finished
   */
  private isGameFinished(gameState: string): boolean {
    const finishedStates = ['OFF', 'FINAL'];
    return finishedStates.includes(gameState.toUpperCase());
  }

  /**
   * Check if game is scheduled (future)
   */
  private isGameScheduled(gameState: string): boolean {
    const scheduledStates = ['FUT', 'SCHEDULED'];
    return scheduledStates.includes(gameState.toUpperCase());
  }

  /**
   * Clear cache for specific game or all games
   */
  clearCache(gameId?: string) {
    if (gameId) {
      this.cache.delete(gameId);
      logger.debug(`Cleared cache for game ${gameId}`);
    } else {
      this.cache.clear();
      logger.debug('Cleared all game cache');
    }
  }

  /**
   * Get time until game starts (in milliseconds)
   * @param startTimeUTC - ISO 8601 UTC timestamp
   * @returns milliseconds until game starts (negative if already started)
   */
  getTimeUntilStart(startTimeUTC: string): number {
    const startTime = new Date(startTimeUTC).getTime();
    const now = Date.now();
    return startTime - now;
  }

  /**
   * Determine optimal polling interval based on game state and time until start
   * @param gameStatus - Current game status
   * @returns Polling interval in milliseconds
   */
  getPollingInterval(gameStatus: GameStatus): number {
    if (gameStatus.isLive) {
      return 30000; // 30 seconds during live game
    }

    if (gameStatus.isFinished) {
      return 300000; // 5 minutes for finished games (in case of corrections)
    }

    // For scheduled games, check time until start
    const timeUntilStart = this.getTimeUntilStart(gameStatus.startTimeUTC);

    if (timeUntilStart < 0) {
      // Game should have started but not marked live yet
      return 60000; // 1 minute
    }

    if (timeUntilStart < 10 * 60 * 1000) {
      // Within 10 minutes of start
      return 60000; // 1 minute
    }

    if (timeUntilStart < 60 * 60 * 1000) {
      // Within 1 hour of start
      return 5 * 60000; // 5 minutes
    }

    // More than 1 hour away
    return 10 * 60000; // 10 minutes
  }
}

// Export singleton instance
export const nhlService = new NHLService();
export default nhlService;
