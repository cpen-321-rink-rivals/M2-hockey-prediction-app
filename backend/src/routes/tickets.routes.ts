import { Router } from "express";
import { createBingoTicket, getUserTickets, deleteTicket } from "../controllers/tickets.controller";

const router = Router();

router.post("/", createBingoTicket);
router.get("/:userId", getUserTickets);
router.delete("/:id", deleteTicket);

export default router;
