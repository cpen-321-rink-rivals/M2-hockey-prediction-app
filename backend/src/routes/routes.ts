import { Router } from 'express';

import { authenticateToken } from '../middleware/auth.middleware';
import authRoutes from './auth.routes';
import mediaRoutes from './media.routes';
import usersRoutes from '../routes/user.routes';
import ticketsRoutes from './tickets.routes';
import friendRoutes from './friends.routes';
import challengesRoutes from './challenges.routes';

const router = Router();

router.use('/auth', authRoutes);

router.use('/user', authenticateToken, usersRoutes);

router.use('/tickets', authenticateToken, ticketsRoutes);

router.use('/challenges', authenticateToken, challengesRoutes);

router.use('/media', authenticateToken, mediaRoutes);

router.use('/friends', authenticateToken, friendRoutes);

export default router;
