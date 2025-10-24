import mongoose, { Schema } from 'mongoose';

const TicketSchema = new Schema({
  userId: { type: String, required: true },
  name: { type: String, required: true },
  game: { type: Object, required: true },
  events: {
    type: [String],
    required: true,
    validate: (v: string | any[]) => v.length === 9,
  },
  createdAt: { type: Date, default: Date.now },
});

export const Ticket = mongoose.model('Ticket', TicketSchema);
