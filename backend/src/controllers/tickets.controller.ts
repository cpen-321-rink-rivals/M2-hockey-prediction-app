import { Request, Response } from 'express';
import { Ticket } from '../models/tickets.model';
import { TicketType } from '../types/tickets.types';

export const createBingoTicket = async (req: Request, res: Response) => {
  try {
    const { userId, name, game, events } = req.body as TicketType;

    if (!userId || !name || !game || !events || events.length !== 9) {
      return res.status(400).json({ message: 'Invalid bingo ticket data' });
    }

    const newTicket = await Ticket.create({ userId, name, game, events });
    res.status(201).json(newTicket);
  } catch (error) {
    console.error('Error creating bingo ticket:', error);
    res.status(500).json({ message: 'Server error' });
  }
};

export const getUserTickets = async (req: Request, res: Response) => {
  try {
    const { userId } = req.params;
    const tickets = await Ticket.find({ userId }).sort({ createdAt: -1 });
    res.json(tickets);
  } catch (error) {
    res.status(500).json({ message: 'Server error' });
  }
};

export const getTicketById = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const ticket = await Ticket.findById(id);
    if (!ticket) {
      return res.status(404).json({ message: 'Ticket not found' });
    }
    res.json(ticket);
  } catch (error) {
    console.error('Error fetching ticket:', error);
    res.status(500).json({ message: 'Server error' });
  }
};

export const deleteTicket = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const deleted = await Ticket.findByIdAndDelete(id);
    if (!deleted) {
      return res.status(404).json({ message: 'Ticket not found' });
    }
    res.json({ message: 'Ticket deleted successfully' });
  } catch (error) {
    res.status(500).json({ message: 'Server error' });
  }
};
