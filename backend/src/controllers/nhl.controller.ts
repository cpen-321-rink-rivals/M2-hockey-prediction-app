import { Request, Response, NextFunction } from 'express';
import nhl from '../nhl.service';

// Vancouver-local YYYY-MM-DD (so “today” matches the API’s date partitions you expect)
function todayVancouverISO(): string {
  const now = new Date();
  // Build as if we were in America/Vancouver
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'America/Vancouver',
    year: 'numeric', month: '2-digit', day: '2-digit',
  }).formatToParts(now).reduce<Record<string,string>>((acc, p) => {
    if (p.type !== 'literal') acc[p.type] = p.value;
    return acc;
  }, {});
  return `${parts.year}-${parts.month}-${parts.day}`;
}

// Map the schedule payload to a simple row like your jq:
// id, startTimeUTC, "AWAY@HOME", venue
function flattenSchedule(schedule: any) {
  if (!schedule?.gameWeek) return [];
  const rows: Array<{ id: number; startTimeUTC: string; matchup: string; venue: string }> = [];
  for (const week of schedule.gameWeek) {
    for (const g of week.games ?? []) {
      rows.push({
        id: g.id,
        startTimeUTC: g.startTimeUTC,
        matchup: `${g.awayTeam?.abbrev ?? '???'}@${g.homeTeam?.abbrev ?? '???'}`,
        venue: g.venue?.default ?? '',
      });
    }
  }
  return rows;
}

/** GET /nhl/games?date=YYYY-MM-DD  (public) */
export const getGamesByDate = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const date = (req.query.date as string) || todayVancouverISO();
    if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) {
      return res.status(400).json({ error: 'Query param "date" must be YYYY-MM-DD' });
    }
    const schedule = await nhl.getSchedule(date);
    const rows = flattenSchedule(schedule);
    return res.json({ date, count: rows.length, games: rows });
  } catch (err) {
    next(err);
  }
};

/** GET /nhl/games/today  (public convenience) */
export const getTodayGames = async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const date = todayVancouverISO();
    const schedule = await nhl.getSchedule(date);
    const rows = flattenSchedule(schedule);
    return res.json({ date, count: rows.length, games: rows });
  } catch (err) {
    next(err);
  }
};

/**
 * POST /nhl/games/:gameId/smoke  (public)
 * Minimal “smoke” call for a game id.
 * If you pass ?verifyToday=true it will confirm the id exists in today’s schedule.
 */
export const smokeGame = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const gameId = Number(req.params.gameId);
    if (!Number.isFinite(gameId)) {
      return res.status(400).json({ error: 'Invalid gameId' });
    }

    const verify = String(req.query.verifyToday ?? 'false').toLowerCase() === 'true';
    let verified = false;

    if (verify) {
      const date = todayVancouverISO();
      const schedule = await nhl.getSchedule(date);
      const rows = flattenSchedule(schedule);
      verified = rows.some(r => r.id === gameId);
      if (!verified) {
        return res.status(404).json({ error: 'Game not found in today’s schedule', date });
      }
    }

    // Skeleton response: echo input & basic status
    return res.status(200).json({
      ok: true,
      gameId,
      verifiedToday: verified,
      message: 'Smoke call successful',
    });
  } catch (err) {
    next(err);
  }
};
