// nhl.types.ts

/** A single, flattened game row returned by your controllers */
export interface GameRow {
  id: number;
  startTimeUTC: string; // ISO string from NHL API
  matchup: string;      // e.g., "NYR@VAN"
  venue: string;
}

/** GET /nhl/games and /nhl/games/today */
export interface GetGamesResponse {
  date: string;        // YYYY-MM-DD (America/Vancouver-local "today" when /today)
  count: number;       // games.length
  games: GameRow[];
}

/** POST /nhl/games/:gameId/smoke */
export interface SmokeGameResponse {
  ok: boolean;
  gameId: number;
  verifiedToday: boolean; // true if ?verifyToday=true and the id is in today's schedule
  message: string;
}

/** Standard error payloads */
export interface ErrorResponse {
  error: string;       // human-readable message
  details?: unknown;   // optional extra info
}

/** Query params & route params for stronger typing on Express handlers */
export interface GetGamesQuery {
  date?: string; // YYYY-MM-DD
}

export interface SmokeGameParams {
  gameId: string; // from req.params
}

export interface SmokeGameQuery {
  verifyToday?: 'true' | 'false';
}
