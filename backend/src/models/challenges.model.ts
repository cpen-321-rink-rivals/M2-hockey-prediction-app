import mongoose, { Schema } from 'mongoose';
import {
  createChallengeSchema,
  IChallenge,
  updateChallengeSchema,
  ChallengeStatus,
  joinChallengeSchema,
} from '../types/challenges.types';
import logger from '../logger.util';
import z from 'zod';

// Type-safe input types derived from Zod schemas
export type CreateChallengeInput = z.infer<typeof createChallengeSchema>;
export type UpdateChallengeInput = z.infer<typeof updateChallengeSchema>;
export type JoinChallengeInput = z.infer<typeof joinChallengeSchema>;

const challengeSchema = new Schema<IChallenge>(
  {
    id: {
      type: String,
      required: true,
      unique: true,
    },
    title: {
      type: String,
      required: true,
      trim: true,
      maxlength: 100,
    },
    description: {
      type: String,
      required: true,
      maxlength: 500,
    },
    ownerId: {
      type: String,
      required: true,
    },
    gameId: {
      type: String,
      required: true,
    },
    status: {
      type: String,
      enum: Object.values(ChallengeStatus),
      default: ChallengeStatus.PENDING,
    },
    memberIds: {
      type: [String],
      default: [],
    },
    invitedUserIds: {
      type: [String],
      default: [],
    },
    maxMembers: {
      type: Number,
      min: 2,
      max: 50,
    },
    ticketIds: {
      type: Map,
      of: String,
      default: new Map(),
    },
    gameStartTime: {
      type: Date,
    },
  },
  {
    timestamps: true, // Automatically adds createdAt and updatedAt
  }
);

export class ChallengesModel {
  private challenge: mongoose.Model<IChallenge>;

  constructor() {
    this.challenge = mongoose.model<IChallenge>('Challenge', challengeSchema);
  }

  // Create a new challenge
  async create(
    data: CreateChallengeInput,
    ownerId: string
  ): Promise<IChallenge> {
    try {
      const validated = createChallengeSchema.parse(data);
      const challengeData = {
        ...validated,
        id: new mongoose.Types.ObjectId().toString(),
        ownerId,
        status: ChallengeStatus.PENDING,
        memberIds: [ownerId], // Owner is automatically a member
      };

      const challenge = new this.challenge(challengeData);
      return await challenge.save();
    } catch (error) {
      if (error instanceof z.ZodError) {
        logger.error('Validation error:', error.issues);
        throw new Error('Invalid challenge data');
      }
      logger.error('Error creating challenge:', error);
      throw new Error('Failed to create challenge');
    }
  }

  // Update an existing challenge
  async update(
    id: string,
    data: Partial<IChallenge>
  ): Promise<IChallenge | null> {
    try {
      const validated = updateChallengeSchema.parse(data);
      const updated = await this.challenge.findOneAndUpdate({ id }, validated, {
        new: true,
      });
      return updated;
    } catch (error) {
      if (error instanceof z.ZodError) {
        logger.error('Validation error:', error.issues);
        throw new Error('Invalid update data');
      }
      logger.error('Error updating challenge:', error);
      throw new Error('Failed to update challenge');
    }
  }

  // Delete a challenge (only by owner)
  async delete(id: string, ownerId: string): Promise<void> {
    try {
      const result = await this.challenge.findOneAndDelete({ id, ownerId });
      if (!result) {
        throw new Error('Challenge not found or not authorized');
      }
    } catch (error) {
      logger.error('Error deleting challenge:', error);
      throw new Error('Failed to delete challenge');
    }
  }

  // Find challenge by ID
  async findById(id: string): Promise<IChallenge | null> {
    try {
      return await this.challenge.findOne({ id });
    } catch (error) {
      logger.error('Error finding challenge by ID:', error);
      throw new Error('Failed to find challenge');
    }
  }

  // Get all challenges (paginated)
  async findAll(
    page: number = 1,
    limit: number = 10
  ): Promise<{ challenges: IChallenge[]; total: number }> {
    try {
      const skip = (page - 1) * limit;
      const [challenges, total] = await Promise.all([
        this.challenge
          .find() // Find all challenges
          .sort({ createdAt: -1 }) // Newest first
          .skip(skip) // Skip previous pages
          .limit(limit), // Take only 'limit' results

        this.challenge.countDocuments(), // Total count for pagination
      ]);
      return { challenges, total };
    } catch (error) {
      logger.error('Error fetching challenges:', error);
      throw new Error('Failed to fetch challenges');
    }
  }

  // Get user's challenges (owner or member)
  async getUserChallenges(
    userId: string,
    status?: string
  ): Promise<IChallenge[]> {
    try {
      // $or means "find documents that match ANY of these conditions":
      const query: any = {
        $or: [
          { ownerId: userId }, // User created this challenge
          { memberIds: userId }, // User joined this challenge
        ],
      };

      if (status) {
        query.status = status; // Add status filter if provided (only active challenges, etc.)
      }

      return await this.challenge.find(query).sort({ createdAt: -1 });
    } catch (error) {
      logger.error('Error fetching user challenges:', error);
      throw new Error('Failed to fetch user challenges');
    }
  }

  // Join a challenge
  async joinChallenge(
    challengeId: string,
    userId: string,
    ticketId: string
  ): Promise<IChallenge | null> {
    try {
      const validated = joinChallengeSchema.parse({ ticketId });

      const challenge = await this.challenge.findOneAndUpdate(
        {
          id: challengeId,
          memberIds: { $nin: [userId] }, // User not already a member
          $or: [
            { maxMembers: { $exists: false } }, // No limit set
            { $expr: { $lt: [{ $size: '$memberIds' }, '$maxMembers'] } }, // Under limit
          ],
        },
        {
          $addToSet: { memberIds: userId },
          $pull: { invitedUserIds: userId },
          $set: { [`ticketIds.${userId}`]: ticketId },
        },
        { new: true }
      );

      if (!challenge) {
        throw new Error(
          'Challenge not found, user already member, or challenge full'
        );
      }

      return challenge;
    } catch (error) {
      if (error instanceof z.ZodError) {
        logger.error('Validation error:', error.issues);
        throw new Error('Invalid ticket data');
      }
      logger.error('Error joining challenge:', error);
      throw new Error('Failed to join challenge');
    }
  }

  // Leave a challenge
  async leaveChallenge(
    challengeId: string,
    userId: string
  ): Promise<IChallenge | null> {
    try {
      const challenge = await this.challenge.findOneAndUpdate(
        {
          id: challengeId,
          memberIds: userId,
          ownerId: { $ne: userId }, // Owner cannot leave, must delete instead $ne = not equal
          status: { $in: [ChallengeStatus.PENDING, ChallengeStatus.ACTIVE] }, // Can't leave live/finished
        },
        {
          $pull: { memberIds: userId },
          $unset: { [`ticketIds.${userId}`]: '' },
        },
        { new: true }
      );

      if (!challenge) {
        throw new Error(
          'Challenge not found, user not member, or cannot leave at this time'
        );
      }

      return challenge;
    } catch (error) {
      logger.error('Error leaving challenge:', error);
      throw new Error('Failed to leave challenge');
    }
  }

  // Update challenge status
  async updateStatus(
    challengeId: string,
    status: string,
    ownerId?: string
  ): Promise<IChallenge | null> {
    try {
      const query: any = { id: challengeId };
      if (ownerId) {
        query.ownerId = ownerId; // Only owner can update status in some cases
      }

      const challenge = await this.challenge.findOneAndUpdate(
        query,
        { status },
        { new: true }
      );

      return challenge;
    } catch (error) {
      logger.error('Error updating challenge status:', error);
      throw new Error('Failed to update challenge status');
    }
  }

  // Get challenges by game ID
  async findByGameId(gameId: string): Promise<IChallenge[]> {
    try {
      return await this.challenge.find({ gameId }).sort({ createdAt: -1 });
    } catch (error) {
      logger.error('Error finding challenges by game ID:', error);
      throw new Error('Failed to find challenges by game ID');
    }
  }
}

export const challengeModel = new ChallengesModel();
