import { Router } from 'express';

import { authenticateToken } from './auth.middleware';
import authRoutes from './auth.routes';
import hobbiesRoutes from './hobbies.routes';
import languagesSpokenRoutes from './languagesSpoken.routes';
import mediaRoutes from './media.routes';
import usersRoutes from './user.routes';

const router = Router();

router.use('/auth', authRoutes);

router.use('/hobbies', authenticateToken, hobbiesRoutes);

router.use('/languages_spoken', authenticateToken, languagesSpokenRoutes);

router.use('/user', authenticateToken, usersRoutes);

router.use('/media', authenticateToken, mediaRoutes);

export default router;
