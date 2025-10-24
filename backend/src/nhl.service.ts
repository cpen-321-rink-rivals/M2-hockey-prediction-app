
import axios from 'axios';
import mongoose, { Schema, model, Document, Model } from 'mongoose';

/**
 * NHL "web" API base. Example: GET /v1/schedule/2025-10-23
 * Docs are unofficial, but the shape is stable enough for our needs.
 */
const NHL_WEB = 'https://api-web.nhle.com/v1';

const http = axios.create({
  baseURL: NHL_WEB,
  timeout: 15_000,
  headers: { Accept: 'application/json', 'User-Agent': 'm3-hockey-app/1.0' },
});

/** Minimal shape we care about for a game */
export interface IGame {
  gamePk: number;              // unique game id (a.k.a id)
  dateISO: string;             // YYYY-MM-DD in America/Vancouver
  startTimeUTC?: string;
  status?: string;
  venue?: string;
  homeTeam: {
    id?: number;
    name?: string;
    abbrev?: string;
  };
  awayTeam: {
    id?: number;
    name?: string;
    abbrev?: string;
  };
}

/** Mongoose model (collection: games). Keep it local to the service to avoid cross-file coupling. */
export interface IGameDoc extends IGame, Document {}
let GameModel: Model<IGameDoc>;

function initModel() {
  if (GameModel) return GameModel;
  const GameSchema = new Schema<IGameDoc>({
    gamePk: { type: Number, required: true, unique: true, index: true },
    dateISO: { type: String, required: true, index: true },
    startTimeUTC: String,
    status: String,
    venue: String,
    homeTeam: {
      id: Number,
      name: String,
      abbrev: String,
    },
    awayTeam: {
      id: Number,
      name: String,
      abbrev: String,
    },
  }, { timestamps: true, versionKey: false });
  GameModel = (mongoose.models.Game || model<IGameDoc>('Game', GameSchema));
  return GameModel;
}

/** Get YYYY-MM-DD for a given timezone (defaults to 'America/Vancouver') */
export function getTodayISO(timeZone = 'America/Vancouver'): string {
  const dt = new Date();
  // Use Intl parts to avoid locale surprises
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(dt);
  const y = parts.find(p => p.type === 'year')?.value;
  const m = parts.find(p => p.type === 'month')?.value;
  const d = parts.find(p => p.type === 'day')?.value;
  return `${y}-${m}-${d}`;
}

/** Shape of NHL /v1/schedule/{dateISO} (we'll be defensive parsing it) */
type RawSchedule = any;

function parseGamesFromSchedule(raw: RawSchedule, dateISO: string): IGame[] {
  if (!raw) return [];
  // Two shapes observed:
  // 1) { gameWeek: [{ games: [ { id, startTimeUTC, venue: { default: name }, homeTeam: { name, abbrev, id }, awayTeam: {...}, gameState } ] }] }
  // 2) { games: [ ...same game objects... ] }
  const dayBlocks: any[] =
    Array.isArray(raw?.gameWeek) ? raw.gameWeek
      : (Array.isArray(raw?.games) ? [{ games: raw.games }] : []);

  const out: IGame[] = [];
  for (const block of dayBlocks) {
    const games = Array.isArray(block?.games) ? block.games : [];
    for (const g of games) {
      const id: number = g?.id ?? g?.gamePk ?? g?.gameId;
        if (!id) continue;
        out.push({
          gamePk: id,
        dateISO,
        startTimeUTC: g?.startTimeUTC ?? g?.startTime ?? g?.startTimeUTCUnix,
        status: g?.gameState ?? g?.gameStatus ?? g?.status,
        venue: g?.venue?.default ?? g?.venue?.name ?? undefined,
        homeTeam: {
          id: g?.homeTeam?.id ?? g?.homeTeam?.teamId,
          name: g?.homeTeam?.name ?? g?.homeTeam?.teamName,
          abbrev: g?.homeTeam?.abbrev ?? g?.homeTeam?.triCode,
        },
        awayTeam: {
          id: g?.awayTeam?.id ?? g?.awayTeam?.teamId,
          name: g?.awayTeam?.name ?? g?.awayTeam?.teamName,
          abbrev: g?.awayTeam?.abbrev ?? g?.awayTeam?.triCode,
        },
      });
    }
  }
  return out;
}

export class NHLService {
  /** GET /v1/schedule/{YYYY-MM-DD} */
  async getSchedule(dateISO: string) {
    const { data } = await http.get(`/schedule/${dateISO}`);
    return data;
  }

  /** Fetch games for a date, parsed to IGame[] */
  async getGamesForDate(dateISO: string): Promise<IGame[]> {
    const raw = await this.getSchedule(dateISO);
    return parseGamesFromSchedule(raw, dateISO);
  }

  /** Ensure we have a Mongoose model ready (and a connection has been established elsewhere) */
  private games() {
    return initModel();
  }

  /**
   * Find the first game for the day that's NOT in the DB yet, insert it, and return it.
   * If all games for the day are already present, returns null.
   */
  async pickAndInsertMissingGameForDate(dateISO: string): Promise<IGameDoc | null> {
    const games = await this.getGamesForDate(dateISO);
    const Game = this.games();

    // Get all existing ids for the day in one query
    const ids = games.map(g => g.gamePk);
    const existing = await Game.find({ gamePk: { $in: ids } }).select('gamePk').lean();
    const existingSet = new Set<number>(existing.map((e: any) => e.gamePk));

    const candidate = games.find(g => !existingSet.has(g.gamePk));
    if (!candidate) return null;

    const created = await Game.create(candidate);
    return created;
  }

  /**
   * Convenience: do the above for "today" in America/Vancouver.
   */
  async pickAndInsertMissingGameForToday(tz = 'America/Vancouver'): Promise<IGameDoc | null> {
    const dateISO = getTodayISO(tz);
    return this.pickAndInsertMissingGameForDate(dateISO);
  }

  /** Helper used by controller smoke-test: upsert a specific game by id if missing */
  async ensureGameById(gamePk: number, dateISO?: string): Promise<IGameDoc> {
    const Game = this.games();
    const found = await Game.findOne({ gamePk });
    if (found) return found;

    // If not provided, infer today's date to fetch schedule where this id should appear.
    const date = dateISO ?? getTodayISO('America/Vancouver');
    const games = await this.getGamesForDate(date);
    const target = games.find(g => g.gamePk === gamePk);
    if (!target) {
      throw new Error(`Game ${gamePk} was not found on ${date}.`);
    }
    const created = await Game.create(target);
    return created;
  }
}

export default new NHLService();
