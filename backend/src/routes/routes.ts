import { Router } from 'express';

import { authenticateToken } from '../auth.middleware';
import authRoutes from './auth.routes';
import hobbiesRoutes from './hobbies.routes';
import languagesSpokenRoutes from '../routes/languagesSpoken.routes';
import mediaRoutes from './media.routes';
import usersRoutes from '../routes/user.routes';
import ticketsRoutes from './tickets.routes';

const router = Router();

router.use('/auth', authRoutes);

router.use('/hobbies', authenticateToken, hobbiesRoutes);

router.use('/languages_spoken', authenticateToken, languagesSpokenRoutes);

router.use('/user', authenticateToken, usersRoutes);

router.use('/tickets', authenticateToken, ticketsRoutes);

router.use('/media', authenticateToken, mediaRoutes);

export default router;
