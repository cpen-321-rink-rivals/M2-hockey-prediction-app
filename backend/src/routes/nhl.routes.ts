// src/routes/nhl.routes.ts
import { Router } from 'express';
import { getGamesByDate, getTeams, getStandings } from '../controllers/nhl.controller';

const router = Router();

/**
 * GET /nhl/games?date=YYYY-MM-DD
 * GET /nhl/teams
 * GET /nhl/standings
 */
router.get('/games', getGamesByDate);
router.get('/teams', getTeams);
router.get('/standings', getStandings);

export default router;
