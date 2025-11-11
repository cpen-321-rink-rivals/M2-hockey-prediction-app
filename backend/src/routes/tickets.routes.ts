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
import { asyncHandler } from '../middleware/async.middleware';

const router = Router();

// Validate the request body for ticket creation so we return 400 on bad input
router.post(
  '/',
  validateBody(createTicketSchema),
  asyncHandler(createBingoTicket)
);
router.get('/user/:userId', asyncHandler(getUserTickets)); // Changed to /user/:userId to avoid conflict
router.get('/:id', asyncHandler(getTicketById)); // Get ticket by ID
router.delete('/:id', asyncHandler(deleteTicket));
router.put('/crossedOff/:id', asyncHandler(updateCrossedOff));

export default router;
