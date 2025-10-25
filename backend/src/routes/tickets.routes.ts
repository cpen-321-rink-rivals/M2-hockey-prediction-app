import { Router } from 'express';
import {
  createBingoTicket,
  getUserTickets,
  getTicketById,
  deleteTicket,
} from '../controllers/tickets.controller';

const router = Router();

router.post('/', createBingoTicket);
router.get('/user/:userId', getUserTickets); // Changed to /user/:userId to avoid conflict
router.get('/:id', getTicketById); // Get ticket by ID
router.delete('/:id', deleteTicket);

export default router;
