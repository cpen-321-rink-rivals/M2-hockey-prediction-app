import z from 'zod';

// Challenge Status Enum - provides type safety and consistency
export const ChallengeStatus = {
  PENDING: 'pending', // Challenge created, waiting for members
  ACTIVE: 'active', // Challenge has members, game not started
  LIVE: 'live', // Game is currently happening
  FINISHED: 'finished', // Game completed
  CANCELLED: 'cancelled', // Challenge cancelled by owner
} as const;

export type ChallengeStatusType =
  (typeof ChallengeStatus)[keyof typeof ChallengeStatus];

// Invitation Status Enum
export const InvitationStatus = {
  PENDING: 'pending',
  ACCEPTED: 'accepted',
  DECLINED: 'declined',
} as const;

export type InvitationStatusType =
  (typeof InvitationStatus)[keyof typeof InvitationStatus];

// Enhanced Challenge Interface
export interface IChallenge {
  id: string;
  title: string;
  description: string;
  ownerId: string;
  gameId: string; // The hockey game this challenge is for
  status: ChallengeStatusType;
  memberIds: string[];
  memberNames: string[];
  invitedUserIds: string[];
  invitedUserNames: string[];
  maxMembers?: number; // Optional member limit
  ticketIds: { [userId: string]: string }; // Map user to their ticket e.g. { userId: ticketId }
  createdAt: Date;
  updatedAt: Date;
  gameStartTime?: Date; // When the hockey game starts
}

// Invitation interface for managing invites
export interface IChallengeInvitation {
  challengeId: string;
  invitedUserId: string;
  invitedByUserId: string;
  status: InvitationStatusType;
  createdAt: Date;
  updatedAt: Date;
}

// Validation Schemas
export const createChallengeSchema = z.object({
  title: z.string().min(1, 'Title is required').max(100, 'Title too long'),
  description: z
    .string()
    .min(1, 'Description is required')
    .max(500, 'Description too long'),
  gameId: z.string().min(1, 'Game ID is required'),
  invitedUserIds: z.array(z.string()).default([]),
  maxMembers: z.number().min(2).max(50).optional(),
  gameStartTime: z.date().optional(),
  ticketId: z.string().optional(), // Owner's selected ticket
});

export const updateChallengeSchema = z.object({
  title: z.string().min(1).max(100).optional(),
  description: z.string().min(1).max(500).optional(),
  status: z
    .enum([
      ChallengeStatus.PENDING,
      ChallengeStatus.ACTIVE,
      ChallengeStatus.LIVE,
      ChallengeStatus.FINISHED,
      ChallengeStatus.CANCELLED,
    ])
    .optional(),
  maxMembers: z.number().min(2).max(50).optional(),
});

export const joinChallengeSchema = z.object({
  ticketId: z.string().min(1, 'Ticket ID is required'),
});

export const inviteUserSchema = z.object({
  userIds: z.array(z.string().min(1)).min(1, 'At least one user ID required'),
});
