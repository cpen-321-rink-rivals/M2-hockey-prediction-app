import { NextFunction, Request, Response } from 'express';

import logger from '../logger.util';
import { MediaService } from '../media.service';
import {
  GetProfileResponse,
  PublicUserInfo,
  UpdateProfileRequest,
} from '../types/user.types';
import { userModel } from '../models/user.model';

export class UserController {
  // get current user's profile
  getProfile(req: Request, res: Response<GetProfileResponse>) {
    const user = req.user!;

    res.status(200).json({
      message: 'Profile fetched successfully',
      data: { user },
    });
  }

  // get user by id
  async getUserInfoById(
    req: Request,
    res: Response<{ message: string; data?: { userInfo: PublicUserInfo } }>,
    next: NextFunction
  ) {
    try {
      const userId = req.params.id;
      const userInfo = await userModel.findUserInfoById(userId);

      if (!userInfo) {
        return res.status(404).json({
          message: 'User info not found',
        });
      }

      res.status(200).json({
        message: 'User info fetched successfully',
        data: { userInfo },
      });
    } catch (error) {
      logger.error('Failed to fetch user info by ID:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to fetch user info by ID',
        });
      }

      next(error);
    }
  }

  async updateProfile(
    req: Request<unknown, unknown, UpdateProfileRequest>,
    res: Response<GetProfileResponse>,
    next: NextFunction
  ) {
    try {
      const user = req.user!;

      const updatedUser = await userModel.update(user._id, req.body);

      if (!updatedUser) {
        return res.status(404).json({
          message: 'User not found',
        });
      }

      res.status(200).json({
        message: 'User info updated successfully',
        data: { user: updatedUser },
      });
    } catch (error) {
      logger.error('Failed to update user info:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to update user info',
        });
      }

      next(error);
    }
  }

  async deleteProfile(req: Request, res: Response, next: NextFunction) {
    try {
      const user = req.user!;

      await MediaService.deleteAllUserImages(user._id.toString());

      await userModel.delete(user._id);

      res.status(200).json({
        message: 'User deleted successfully',
      });
    } catch (error) {
      logger.error('Failed to delete user:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to delete user',
        });
      }

      next(error);
    }
  }
}
