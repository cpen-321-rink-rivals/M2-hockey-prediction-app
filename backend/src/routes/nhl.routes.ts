import { Router } from 'express';
import { getGamesByDate, getTodayGames, smokeGame } from '../controllers/nhl.controller';

const router = Router();

/**
 * GET /nhl/games?date=YYYY-MM-DD   → defaults to today (America/Vancouver)
 * GET /nhl/games/today
 * POST /nhl/games/:gameId/smoke    → optional ?verifyToday=true
 */
router.get('/games', getGamesByDate);
router.get('/games/today', getTodayGames);
router.post('/games/:gameId/smoke', smokeGame);

export default router;
