import mongoose, { Schema } from 'mongoose';

const EventConditionSchema = new Schema({
  id: { type: String, required: true },
  category: { type: String, required: true },       // store enum as string
  subject: { type: String, required: true },
  comparison: { type: String, required: true },     // store enum as string
  threshold: { type: Number, required: true },
  teamAbbrev: { type: String },
  playerId: { type: Number },
  playerName: { type: String },
}, { _id: false }); // disable separate _id for each subdocument

const TicketSchema = new Schema({
  userId: { type: String, required: true },
  name: { type: String, required: true },
  game: { type: Object, required: true },
  events: {
    type: [EventConditionSchema],
    required: true,
    validate: (v: any[]) => v.length === 9,
  },
  crossedOff: {
    type: [Boolean],
    required: true,
    default: Array(9).fill(false),
    validate: (v: boolean[]) => v.length === 9,
  },
  createdAt: { type: Date, default: Date.now },
});

export const Ticket = mongoose.model('Ticket', TicketSchema);
