import mongoose, { Document } from 'mongoose';
import z from 'zod';

export type ScheduleResponse = {
  nextStartDate: string;
  previousStartDate: string;
  gameDay: GameDay[];
};

export type GameDay = {
  date: string;
  dayAbbrev: string;
  numberOfGames: number;
  datePromo: any[]; // Could refine if you know the type
  games: Game[];
};

export type Game = {
  id: number;
  season: number;
  gameType: number;
  venue: Venue;
  neutralSite: boolean;
  startTimeUTC: string;
  easternUTCOffset: string;
  venueUTCOffset: string;
  venueTimezone: string;
  gameState: string;
  gameScheduleState: string;
  tvBroadcasts: TvBroadcast[];
  awayTeam: Team;
  homeTeam: Team;
  periodDescriptor: PeriodDescriptor;
  ticketsLink?: string;
  ticketsLinkFr?: string;
  gameCenterLink?: string;
};

export type Venue = {
  default: string;
  fr?: string;
};

export type TvBroadcast = {
  id: number;
  market: string;
  countryCode: string;
  network: string;
  sequenceNumber: number;
};

export type Team = {
  id: number;
  commonName: Name;
  placeName: Name;
  placeNameWithPreposition: Name;
  abbrev: string;
  logo: string;
  darkLogo: string;
  awaySplitSquad?: boolean;
  homeSplitSquad?: boolean;
  radioLink: string;
  odds?: Odds[];
};

export type Name = {
  default: string;
  fr?: string;
};

export type Odds = {
  providerId: number;
  value: string;
};

export type PeriodDescriptor = {
  number: number;
  periodType: string;
  maxRegulationPeriods: number;
};

// Interface used internally by Mongoose
export interface ITicket extends Document {
  userId: string;
  name: string;
  game: Game;
  events: string[];
  createdAt: Date;
  updatedAt: Date;
}

// Zod schema for validation when creating tickets
export const createTicketSchema = z.object({
  userId: z.string().min(1, 'User ID required'),
  name: z.string().min(1, 'Name required'),
  game: z.object({
    id: z.number(),
    homeTeam: z.object({
      abbrev: z.string().min(1, 'Home team abbrev required'),
    }),
    awayTeam: z.object({
      abbrev: z.string().min(1, 'Away team abbrev required'),
    }),
  }),
  events: z.array(z.string()).length(9, 'Exactly 9 events required'),
});

export type CreateTicketBody = z.infer<typeof createTicketSchema>;

// Optional: maintain compatibility with your previous naming
export type TicketType = CreateTicketBody;
