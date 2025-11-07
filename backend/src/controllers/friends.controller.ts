import { Request, Response, NextFunction } from 'express';
import { friendModel } from '../models/friends.model';
import { userModel } from '../models/user.model';
import { SendFriendRequestBody } from '../types/friends.types';
import logger from '../utils/logger.util';

export class FriendController {
  async sendRequest(
    req: Request<unknown, unknown, SendFriendRequestBody>,
    res: Response,
    next: NextFunction
  ) {
    try {
      const sender = req.user!;
      const { receiverCode } = req.body;

      const receiver = await userModel.findByFriendCode(receiverCode);
      if (!receiver) {
        return res.status(404).json({ message: 'User not found' });
      }

      const request = await friendModel.sendRequest(
        sender._id.toString(),
        receiver._id.toString()
      );
      res.status(201).json({ message: 'Friend request sent', data: request });
    } catch (error) {
      logger.error('Error sending friend request:', error);
      next(error);
    }
  }

  async acceptRequest(req: Request, res: Response, next: NextFunction) {
    try {
      const { requestId } = req.body;

      if (!requestId) {
        return res.status(400).json({ message: 'Request ID is required' });
      }

      const updated = await friendModel.acceptRequest(requestId);
      res
        .status(200)
        .json({ message: 'Friend request accepted', data: updated });
    } catch (error) {
      next(error);
    }
  }

  async rejectRequest(req: Request, res: Response, next: NextFunction) {
    try {
      const { requestId } = req.body;

      if (!requestId) {
        return res.status(400).json({ message: 'Request ID is required' });
      }

      const updated = await friendModel.rejectRequest(requestId);
      res
        .status(200)
        .json({ message: 'Friend request rejected', data: updated });
    } catch (error) {
      next(error);
    }
  }

  async getFriends(req: Request, res: Response, next: NextFunction) {
    try {
      const user = req.user!;
      const friends = await friendModel.getFriends(user._id.toString());
      res
        .status(200)
        .json({ message: 'Friends fetched successfully', data: friends });
    } catch (error) {
      next(error);
    }
  }

  async getPendingRequests(req: Request, res: Response, next: NextFunction) {
    try {
      const user = req.user!;
      const requests = await friendModel.getPendingRequests(
        user._id.toString()
      );

      res.status(200).json({
        message: 'Pending requests fetched successfully',
        data: requests,
      });
    } catch (error) {
      next(error);
    }
  }

  async removeFriend(req: Request, res: Response, next: NextFunction) {
    try {
      const user = req.user!;
      const { friendId } = req.params;
      await friendModel.removeFriend(user._id.toString(), friendId);
      res.status(200).json({ message: 'Friend removed successfully' });
    } catch (error) {
      next(error);
    }
  }
}
