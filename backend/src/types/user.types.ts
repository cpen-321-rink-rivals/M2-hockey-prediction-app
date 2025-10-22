import mongoose, { Document } from 'mongoose';
import z from 'zod';
import { HOBBIES } from '../hobbies';
import { LANGUAGES_SPOKEN } from '../languagesSpoken';

// User model
// ------------------------------------------------------------
export interface IUser extends Document {
  _id: mongoose.Types.ObjectId;
  googleId: string;
  email: string;
  name: string;
  profilePicture?: string;
  bio?: string;
  hobbies: string[];
  languagesSpoken: string[];
  createdAt: Date;
  updatedAt: Date;
  friendCode: string;
}

// Zod schemas
// ------------------------------------------------------------
export const createUserSchema = z.object({
  email: z.string().email(),
  name: z.string().min(1),
  googleId: z.string().min(1),
  profilePicture: z.string().optional(),
  bio: z.string().max(500).optional(),
  hobbies: z.array(z.string()).default([]),
  languagesSpoken: z.array(z.string()).default([]),
  friendCode: z.string().optional(),
});

export const updateProfileSchema = z.object({
  name: z.string().min(1).optional(),
  bio: z.string().max(500).optional(),
  hobbies: z
    .array(z.string())
    .refine(val => val.length === 0 || val.every(v => HOBBIES.includes(v)), {
      message: 'Hobby must be in the available hobbies list',
    })
    .optional(),
  languagesSpoken: z
    .array(z.string())
    .refine(
      val => val.length === 0 || val.every(v => LANGUAGES_SPOKEN.includes(v)),
      {
        message: 'Language spoken must be in the available list',
      }
    )
    .optional(),
  profilePicture: z.string().min(1).optional(),
});

// Request types
// ------------------------------------------------------------
export type GetProfileResponse = {
  message: string;
  data?: {
    user: IUser;
  };
};

export type UpdateProfileRequest = z.infer<typeof updateProfileSchema>;

// Generic types
// ------------------------------------------------------------
export type GoogleUserInfo = {
  googleId: string;
  email: string;
  name: string;
  profilePicture?: string;
};

export type PublicUserInfo = {
  _id: mongoose.Types.ObjectId;
  name: string;
  profilePicture?: string;
  bio?: string;
  hobbies: string[];
  friendCode: string;
};
