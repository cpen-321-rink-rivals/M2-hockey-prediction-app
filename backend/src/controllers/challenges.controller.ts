import { NextFunction, Request, Response } from 'express';
import logger from '../logger.util';
import id from 'zod/v4/locales/id.js';

export class ChallengesController {
  async getAllChallenges(req: Request, res: Response, next: NextFunction) {
    try {
      // dummy data, replace with actual service call
      const challenges = [
        {
          id: 1,
          title: 'Sample Challenge 1',
          description: 'This is a sample challenge',
        },
        {
          id: 2,
          title: 'Sample Challenge 2',
          description: 'This is another sample challenge',
        },
      ];

      res.status(200).json({
        message: 'Challenges fetched successfully',
        data: challenges,
      });
    } catch (error) {
      logger.error('Failed to fetch challenges:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to fetch challenges',
        });
      }

      next(error);
    }
  }
}
