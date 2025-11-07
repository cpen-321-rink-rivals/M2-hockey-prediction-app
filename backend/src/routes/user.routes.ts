import { Router } from 'express';

import { UserController } from '../controllers/user.controller';
import { UpdateProfileRequest, updateProfileSchema } from '../types/user.types';
import { validateBody } from '../middleware/validation.middleware';

const router = Router();
const userController = new UserController();

// GET /user/profile - Get current user's profile
router.get('/profile', userController.getProfile);

// GET /user/:id - Get user info by ID
router.get('/:id', userController.getUserInfoById);

// PUT /user/profile - Update current user's profile
router.put(
  '/profile',
  validateBody<UpdateProfileRequest>(updateProfileSchema),
  userController.updateProfile
);

// DELETE /user/profile - Delete current user's profile
router.delete('/profile', userController.deleteProfile);

export default router;
