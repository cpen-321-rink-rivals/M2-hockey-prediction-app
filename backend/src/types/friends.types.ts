import mongoose, { Document } from 'mongoose';
import z from 'zod';

export interface IFriendRequest extends Document {
  sender: mongoose.Types.ObjectId;
  receiver: mongoose.Types.ObjectId;
  status: 'pending' | 'accepted' | 'rejected';
  createdAt: Date;
  updatedAt: Date;
}

export const sendFriendRequestSchema = z.object({
  receiverCode: z.string().min(1, 'Friend code required'),
});

export type SendFriendRequestBody = z.infer<typeof sendFriendRequestSchema>;
