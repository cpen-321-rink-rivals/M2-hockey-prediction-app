import { Request, Response } from 'express';
import logger from '../logger.util';
import { challengeModel } from '../models/challenges.model';
import { ChallengeStatus } from '../types/challenges.types';

export class ChallengesController {
  // Create a new challenge
  async create(req: Request, res: Response) {
    try {
      if (!req.user || !req.user.id) {
        return res.status(401).json({
          error: 'Unauthorized',
          message: 'User authentication required',
        });
      }

      const challenge = await challengeModel.create(req.body, req.user.id);
      logger.info(`Challenge created: ${challenge.id} by user: ${req.user.id}`);

      res.status(201).json({
        success: true,
        data: challenge,
        message: 'Challenge created successfully',
      });
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'An error occurred';
      logger.error(`Error creating challenge: ${message}`);
      res.status(400).json({
        error: 'Bad Request',
        message,
      });
    }
  }

  // Get all challenges (with pagination)
  async getAll(req: Request, res: Response) {
    try {
      const page = parseInt(req.query.page as string) || 1;
      const limit = parseInt(req.query.limit as string) || 10;

      const result = await challengeModel.findAll(page, limit);

      res.status(200).json({
        success: true,
        data: result.challenges,
        pagination: {
          page,
          limit,
          total: result.total,
          pages: Math.ceil(result.total / limit),
        },
      });
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'An error occurred';
      logger.error(`Error fetching challenges: ${message}`);
      res.status(500).json({
        error: 'Internal Server Error',
        message,
      });
    }
  }

  // Get challenge by ID
  async getById(req: Request, res: Response) {
    try {
      const { id } = req.params;
      const challenge = await challengeModel.findById(id);

      if (!challenge) {
        return res.status(404).json({
          error: 'Not Found',
          message: 'Challenge not found',
        });
      }

      res.status(200).json({
        success: true,
        data: challenge,
      });
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'An error occurred';
      logger.error(`Error fetching challenge by ID: ${message}`);
      res.status(500).json({
        error: 'Internal Server Error',
        message,
      });
    }
  }

  // Get user's challenges
  async getUserChallenges(req: Request, res: Response) {
    try {
      if (!req.user || !req.user.id) {
        return res.status(401).json({
          error: 'Unauthorized',
          message: 'User authentication required',
        });
      }

      const status = req.query.status as string;
      const challenges = await challengeModel.getUserChallenges(
        req.user.id,
        status
      );

      // Group challenges by status for easier frontend consumption
      const groupedChallenges = {
        pending: challenges.filter(c => c.status === ChallengeStatus.PENDING),
        active: challenges.filter(c => c.status === ChallengeStatus.ACTIVE),
        live: challenges.filter(c => c.status === ChallengeStatus.LIVE),
        finished: challenges.filter(c => c.status === ChallengeStatus.FINISHED),
        cancelled: challenges.filter(
          c => c.status === ChallengeStatus.CANCELLED
        ),
      };

      res.status(200).json({
        success: true,
        data: status ? challenges : groupedChallenges,
        total: challenges.length,
      });
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'An error occurred';
      logger.error(`Error fetching user challenges: ${message}`);
      res.status(500).json({
        error: 'Internal Server Error',
        message,
      });
    }
  }

  // Join a challenge
  async joinChallenge(req: Request, res: Response) {
    try {
      if (!req.user || !req.user.id) {
        return res.status(401).json({
          error: 'Unauthorized',
          message: 'User authentication required',
        });
      }

      const { id } = req.params;
      const { ticketId } = req.body;

      const challenge = await challengeModel.joinChallenge(
        id,
        req.user.id,
        ticketId
      );

      if (!challenge) {
        return res.status(400).json({
          error: 'Bad Request',
          message:
            'Unable to join challenge. It may be full, you may already be a member, or it may not exist.',
        });
      }

      logger.info(`User ${req.user.id} joined challenge ${id}`);

      res.status(200).json({
        success: true,
        data: challenge,
        message: 'Successfully joined challenge',
      });
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'An error occurred';
      logger.error(`Error joining challenge: ${message}`);
      res.status(400).json({
        error: 'Bad Request',
        message,
      });
    }
  }

  // Leave a challenge
  async leaveChallenge(req: Request, res: Response) {
    try {
      if (!req.user || !req.user.id) {
        return res.status(401).json({
          error: 'Unauthorized',
          message: 'User authentication required',
        });
      }

      const { id } = req.params;
      const challenge = await challengeModel.leaveChallenge(id, req.user.id);

      if (!challenge) {
        return res.status(400).json({
          error: 'Bad Request',
          message:
            'Unable to leave challenge. You may not be a member, be the owner, or the challenge may be live/finished.',
        });
      }

      logger.info(`User ${req.user.id} left challenge ${id}`);

      res.status(200).json({
        success: true,
        data: challenge,
        message: 'Successfully left challenge',
      });
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'An error occurred';
      logger.error(`Error leaving challenge: ${message}`);
      res.status(400).json({
        error: 'Bad Request',
        message,
      });
    }
  }

  // Update challenge
  async update(req: Request, res: Response) {
    try {
      if (!req.user || !req.user.id) {
        return res.status(401).json({
          error: 'Unauthorized',
          message: 'User authentication required',
        });
      }

      const { id } = req.params;

      // First check if user owns the challenge
      const existingChallenge = await challengeModel.findById(id);
      if (!existingChallenge) {
        return res.status(404).json({
          error: 'Not Found',
          message: 'Challenge not found',
        });
      }

      if (existingChallenge.ownerId !== req.user.id) {
        return res.status(403).json({
          error: 'Forbidden',
          message: 'Only challenge owner can update challenge',
        });
      }

      const challenge = await challengeModel.update(id, req.body);

      logger.info(`Challenge ${id} updated by user ${req.user.id}`);

      res.status(200).json({
        success: true,
        data: challenge,
        message: 'Challenge updated successfully',
      });
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'An error occurred';
      logger.error(`Error updating challenge: ${message}`);
      res.status(400).json({
        error: 'Bad Request',
        message,
      });
    }
  }

  // Delete challenge
  async delete(req: Request, res: Response) {
    try {
      if (!req.user || !req.user.id) {
        return res.status(401).json({
          error: 'Unauthorized',
          message: 'User authentication required',
        });
      }

      const { id } = req.params;
      await challengeModel.delete(id, req.user.id);

      logger.info(`Challenge ${id} deleted by user ${req.user.id}`);

      res.status(200).json({
        success: true,
        message: 'Challenge deleted successfully',
      });
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'An error occurred';
      logger.error(`Error deleting challenge: ${message}`);
      res.status(400).json({
        error: 'Bad Request',
        message,
      });
    }
  }

  // Update challenge status (for system updates)
  async updateStatus(req: Request, res: Response) {
    try {
      const { id } = req.params;
      const { status } = req.body;

      if (!Object.values(ChallengeStatus).includes(status)) {
        return res.status(400).json({
          error: 'Bad Request',
          message: 'Invalid status value',
        });
      }

      const challenge = await challengeModel.updateStatus(id, status);

      if (!challenge) {
        return res.status(404).json({
          error: 'Not Found',
          message: 'Challenge not found',
        });
      }

      logger.info(`Challenge ${id} status updated to ${status}`);

      res.status(200).json({
        success: true,
        data: challenge,
        message: 'Challenge status updated successfully',
      });
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'An error occurred';
      logger.error(`Error updating challenge status: ${message}`);
      res.status(400).json({
        error: 'Bad Request',
        message,
      });
    }
  }

  // Get challenges by game ID
  async getByGameId(req: Request, res: Response) {
    try {
      const { gameId } = req.params;
      const challenges = await challengeModel.findByGameId(gameId);

      res.status(200).json({
        success: true,
        data: challenges,
        total: challenges.length,
      });
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'An error occurred';
      logger.error(`Error fetching challenges by game ID: ${message}`);
      res.status(500).json({
        error: 'Internal Server Error',
        message,
      });
    }
  }
}
