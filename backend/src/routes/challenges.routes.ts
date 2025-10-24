import { ChallengesController } from '../controllers/challenges.controller';
import { Router } from 'express';
import { validateBody } from '../validation.middleware';
import {
  createChallengeSchema,
  updateChallengeSchema,
  joinChallengeSchema,
} from '../types/challenges.types';

const router = Router();
const challengeController = new ChallengesController();

// Public routes (still require authentication from main router)

// GET /challenges - Get all challenges with pagination
router.get('/', challengeController.getAll.bind(challengeController));

// GET /challenges/user - Get current user's challenges - owned, joined, and invited
router.get(
  '/user',
  challengeController.getUserChallenges.bind(challengeController)
);

// GET /challenges/:id - Get specific challenge by ID
router.get('/:id', challengeController.getById.bind(challengeController));

// GET /challenges/game/:gameId - Get challenges for specific game
router.get(
  '/game/:gameId',
  challengeController.getByGameId.bind(challengeController)
);

// POST /challenges - Create new challenge
router.post(
  '/',
  validateBody(createChallengeSchema),
  challengeController.create.bind(challengeController)
);

// PUT /challenges/:id - Update challenge (owner only)
router.put(
  '/:id',
  validateBody(updateChallengeSchema),
  challengeController.update.bind(challengeController)
);

// DELETE /challenges/:id - Delete challenge (owner only)
router.delete('/:id', challengeController.delete.bind(challengeController));

// POST /challenges/:id/join - Join a challenge
router.post(
  '/:id/join',
  validateBody(joinChallengeSchema),
  challengeController.joinChallenge.bind(challengeController)
);

// POST /challenges/:id/leave - Leave a challenge
router.post(
  '/:id/leave',
  challengeController.leaveChallenge.bind(challengeController)
);

// POST /challenges/:id/decline - Decline a challenge invitation
router.post(
  '/:id/decline',
  challengeController.declineInvitation.bind(challengeController)
);

// PATCH /challenges/:id/status - Update challenge status (system/admin use)
router.patch(
  '/:id/status',
  challengeController.updateStatus.bind(challengeController)
);

export default router;
