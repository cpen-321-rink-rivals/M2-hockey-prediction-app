import { TicketsController } from '../controllers/tickets.controller';
import { Router } from 'express';

const router = Router();
const ticketsController = new TicketsController();

router.get('/', ticketsController.getAllTickets);

export default router;
