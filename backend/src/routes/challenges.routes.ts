import { ChallengesController } from '../controllers/challenges.controller';
import { Router } from 'express';

const router = Router();
const challengeController = new ChallengesController();

router.get('/', challengeController.getAllChallenges);

export default router;
