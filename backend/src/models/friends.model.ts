import mongoose, { Schema, Document } from 'mongoose';
import { IFriendRequest } from '../types/friends.types';

const friendRequestSchema = new Schema<IFriendRequest>(
  {
    sender: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: true,
    },
    receiver: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: true,
    },
    status: {
      type: String,
      enum: ['pending', 'accepted', 'rejected'],
      default: 'pending',
    },
  },
  { timestamps: true }
);

class FriendModel {
  private friendRequest: mongoose.Model<IFriendRequest>;

  constructor() {
    this.friendRequest = mongoose.model<IFriendRequest>('FriendRequest', friendRequestSchema);
  }

  async sendRequest(senderId: string, receiverId: string) {
    const existing = await this.friendRequest.findOne({
      sender: senderId,
      receiver: receiverId,
    });

    if (existing) {
      throw new Error('Friend request already exists');
    }

    return this.friendRequest.create({ sender: senderId, receiver: receiverId });
  }

  async acceptRequest(requestId: string) {
    return this.friendRequest.findByIdAndUpdate(
      requestId,
      { status: 'accepted' },
      { new: true }
    );
  }

  async rejectRequest(requestId: string) {
    return this.friendRequest.findByIdAndUpdate(
      requestId,
      { status: 'rejected' },
      { new: true }
    );
  }

  async getFriends(userId: string) {
    return this.friendRequest
      .find({
        status: 'accepted',
        $or: [{ sender: userId }, { receiver: userId }],
      })
      .populate('sender receiver', 'name email profilePicture');
  }

  async removeFriend(userId: string, friendId: string) {
    return this.friendRequest.findOneAndDelete({
      status: 'accepted',
      $or: [
        { sender: userId, receiver: friendId },
        { sender: friendId, receiver: userId },
      ],
    });
  }

  async getPendingRequests(userId: string) {
    return this.friendRequest
      .find({
        receiver: userId,
        status: 'pending',
      })
      .populate('sender', 'name email profilePicture');
  }
}

export const friendModel = new FriendModel();
