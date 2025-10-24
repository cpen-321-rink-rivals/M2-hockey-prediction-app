// src/controllers/nhl.controller.ts
import { Request, Response, NextFunction } from 'express';
// If your service file is in src/nhl.service.ts:
import NHLService from '../nhl.service'; // <-- change to './nhl.service' if controller is in src/
// If your types file is in src/nhl.types.ts:
import { GetGamesQuery, GetGamesResponse, GameRow } from '../nhl.types'; // or './nhl.types'

/** Minimal view of the service’s game type so we can map safely */
type IGameLike = {
  gamePk: number;
  startTimeUTC?: string;
  gameDate?: string;
  // Common fields seen in NHL API/service mappers
  away?: { abbrev?: string } | null;
  home?: { abbrev?: string } | null;
  awayTeamAbbrev?: string;
  homeTeamAbbrev?: string;
  awayTriCode?: string;
  homeTriCode?: string;
  venue?: { name?: string } | string | null;
};

function pickAbbrev(
  g: IGameLike,
  side: 'away' | 'home'
): string {
  const obj = (g as any)[side];
  return (
    obj?.abbrev ??
    (side === 'away' ? g.awayTeamAbbrev : g.homeTeamAbbrev) ??
    (side === 'away' ? g.awayTriCode : g.homeTriCode) ??
    '???'
  );
}

function toRow(g: IGameLike): GameRow {
  const start =
    g.startTimeUTC ??
    g.gameDate ??
    ''; // keep empty if unknown; your service should normally provide one

  const venue =
    typeof g.venue === 'string'
      ? g.venue
      : g.venue?.name ?? '';

  return {
    id: g.gamePk,
    startTimeUTC: start,
    matchup: `${pickAbbrev(g, 'away')}@${pickAbbrev(g, 'home')}`,
    venue,
  };
}

/** Resolve "today" in America/Vancouver as YYYY-MM-DD */
function getTodayISO(tz = 'America/Vancouver'): string {
  const now = new Date();
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: tz,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(now);
  const part = (t: string) => parts.find(p => p.type === t)?.value ?? '';
  return `${part('year')}-${part('month')}-${part('day')}`;
}

/**
 * GET /nhl/games?date=YYYY-MM-DD   → defaults to today (America/Vancouver)
 */
export async function getGamesByDate(
  req: Request<{}, GetGamesResponse, {}, GetGamesQuery>,
  res: Response<GetGamesResponse>,
  next: NextFunction
) {
  try {
    const date = req.query.date || getTodayISO();
    const games = (await NHLService.getGamesForDate(date)) as IGameLike[];
    const rows = games.map(toRow);

    const payload: GetGamesResponse = {
      date,
      count: rows.length,
      games: rows, // <-- now matches GameRow[]
    };
    res.json(payload);
  } catch (err) {
    next(err);
  }
}
