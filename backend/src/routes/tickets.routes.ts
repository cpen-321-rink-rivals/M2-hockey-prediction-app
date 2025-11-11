import { Router } from 'express';
import {
  createBingoTicket,
  getUserTickets,
  getTicketById,
  deleteTicket,
  updateCrossedOff,
} from '../controllers/tickets.controller';
import { validateBody } from '../middleware/validation.middleware';
import { createTicketSchema } from '../types/tickets.types';

const router = Router();

// Validate the request body for ticket creation so we return 400 on bad input
router.post('/', validateBody(createTicketSchema), createBingoTicket);
router.get('/user/:userId', getUserTickets); // Changed to /user/:userId to avoid conflict
router.get('/:id', getTicketById); // Get ticket by ID
router.delete('/:id', deleteTicket);
router.put('/crossedOff/:id', updateCrossedOff);

export default router;
