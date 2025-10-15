import { Router } from 'express';

import { authenticateToken } from '../auth.middleware';
import authRoutes from './auth.routes';
import hobbiesRoutes from './hobbies.routes';
import languagesSpokenRoutes from '../routes/languagesSpoken.routes';
import mediaRoutes from './media.routes';
import usersRoutes from '../routes/user.routes';
import ticketsRoutes from './tickets.routes';

// NHL SERVICE
import nhlRoutes from './nhl.routes';

const router = Router();

/** Public healthcheck endpoint (no auth) */
router.get('/ping', (req, res) => {
  res.status(200).json({
    status: 'ok',
    uptime: process.uptime(),
    timestamp: new Date().toISOString(),
  });
});

router.use('/auth', authRoutes);

router.use('/hobbies', authenticateToken, hobbiesRoutes);

router.use('/languages_spoken', authenticateToken, languagesSpokenRoutes);

router.use('/user', authenticateToken, usersRoutes);

router.use('/tickets', authenticateToken, ticketsRoutes);

router.use('/media', authenticateToken, mediaRoutes);

router.use('/nhl', authenticateToken, nhlRoutes);

export default router;
