import mongoose, { Schema } from 'mongoose';
import { z } from 'zod';

import { HOBBIES } from '../hobbies';
import { LANGUAGES_SPOKEN } from '../languagesSpoken';
import {
  createUserSchema,
  GoogleUserInfo,
  IUser,
  PublicUserInfo,
  updateProfileSchema,
} from '../types/user.types';
import logger from '../logger.util';

const userSchema = new Schema<IUser>(
  {
    googleId: {
      type: String,
      required: true,
      unique: true,
      index: true,
    },
    email: {
      type: String,
      required: true,
      unique: true,
      lowercase: true,
      trim: true,
    },
    name: {
      type: String,
      required: true,
      trim: true,
    },
    profilePicture: {
      type: String,
      required: false,
      trim: true,
    },
    bio: {
      type: String,
      required: false,
      trim: true,
      maxlength: 500,
    },
    hobbies: {
      type: [String],
      default: [],
      validate: {
        validator: function (hobbies: string[]) {
          return (
            hobbies.length === 0 ||
            hobbies.every(hobby => HOBBIES.includes(hobby))
          );
        },
        message:
          'Hobbies must be non-empty strings and must be in the available hobbies list',
      },
    },
    languagesSpoken: {
      type: [String],
      default: [],
      validate: {
        validator: function (languages: string[]) {
          return (
            languages.length === 0 ||
            languages.every(lang => LANGUAGES_SPOKEN.includes(lang))
          );
        },
        message: 'Languages spoken must be in the available languages list',
      },
    },
    friendCode: {
      type: String,
      required: true,
      unique: true,
      index: true,
    },
  },
  {
    timestamps: true,
  }
);

function generateFriendCode(length = 6): string {
  const chars = 'ABCDEFGHIJKLMNPQRSTUVWXYZ0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

export class UserModel {
  private user: mongoose.Model<IUser>;

  constructor() {
    this.user = mongoose.model<IUser>('User', userSchema);
  }

  async create(userInfo: GoogleUserInfo): Promise<IUser> {
    try {
      const validatedData = createUserSchema.parse(userInfo);

      let friendCode: string;
      let isUnique = false;
      do {
        friendCode = generateFriendCode();
        const existing = await this.user.findOne({ friendCode });
        if (!existing) isUnique = true;
      } while (!isUnique);

      return await this.user.create({ ...validatedData, friendCode });
    } catch (error) {
      if (error instanceof z.ZodError) {
        console.error('Validation error:', error.issues);
        throw new Error('Invalid update data');
      }
      console.error('Error updating user:', error);
      throw new Error('Failed to update user');
    }
  }

  async update(
    userId: mongoose.Types.ObjectId,
    user: Partial<IUser>
  ): Promise<IUser | null> {
    try {
      const validatedData = updateProfileSchema.parse(user);

      const updatedUser = await this.user.findByIdAndUpdate(
        userId,
        validatedData,
        {
          new: true,
        }
      );
      return updatedUser;
    } catch (error) {
      logger.error('Error updating user:', error);
      throw new Error('Failed to update user');
    }
  }

  async delete(userId: mongoose.Types.ObjectId): Promise<void> {
    try {
      await this.user.findByIdAndDelete(userId);
    } catch (error) {
      logger.error('Error deleting user:', error);
      throw new Error('Failed to delete user');
    }
  }

  async findById(_id: mongoose.Types.ObjectId): Promise<IUser | null> {
    try {
      const user = await this.user.findOne({ _id });

      if (!user) {
        return null;
      }

      return user;
    } catch (error) {
      console.error('Error finding user by Google ID:', error);
      throw new Error('Failed to find user');
    }
  }

  async findUserInfoById(userId: string): Promise<PublicUserInfo | null> {
    try {
      const user = await this.user.findById(userId);

      if (!user) {
        return null;
      }

      // get only public info
      const publicInfo: PublicUserInfo = {
        _id: user._id,
        name: user.name,
        profilePicture: user.profilePicture,
        bio: user.bio,
        hobbies: user.hobbies,
        languagesSpoken: user.languagesSpoken || [],
        friendCode: user.friendCode,
      };

      return publicInfo;
    } catch (error) {
      console.error('Error finding user info by ID:', error);
      throw new Error('Failed to find user info');
    }
  }

  async findByGoogleId(googleId: string): Promise<IUser | null> {
    try {
      const user = await this.user.findOne({ googleId });

      if (!user) {
        return null;
      }

      return user;
    } catch (error) {
      console.error('Error finding user by Google ID:', error);
      throw new Error('Failed to find user');
    }
  }

  async findByFriendCode(code: string): Promise<IUser | null> {
    try {
      const user = await this.user.findOne({ friendCode: code });
      return user || null;
    } catch (error) {
      console.error('Error finding user by friend code:', error);
      throw new Error('Failed to find user');
    }
  }
}

export const userModel = new UserModel();
