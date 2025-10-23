import mongoose, { Document } from 'mongoose';
import z from 'zod';

// Interface used internally by Mongoose
export interface ITicket extends Document {
  userId: string;
  name: string;
  game: string;
  events: string[];
  createdAt: Date;
  updatedAt: Date;
}

// Zod schema for validation when creating tickets
export const createTicketSchema = z.object({
  userId: z.string().min(1, 'User ID required'),
  name: z.string().min(1, 'Name required'),
  game: z.string().min(1, 'Game required'),
  events: z.array(z.string()).length(9, 'Exactly 9 events required'),
});

export type CreateTicketBody = z.infer<typeof createTicketSchema>;

// Optional: maintain compatibility with your previous naming
export type TicketType = CreateTicketBody;
