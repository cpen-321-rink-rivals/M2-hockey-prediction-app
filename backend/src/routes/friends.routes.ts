import { Router } from 'express';
import { FriendController } from '../controllers/friends.controller';
import { validateBody } from '../validation.middleware';
import { sendFriendRequestSchema, SendFriendRequestBody } from '../types/friends.types';

const router = Router();
const friendController = new FriendController();

router.post(
  '/request',
  validateBody<SendFriendRequestBody>(sendFriendRequestSchema),
  friendController.sendRequest
);

router.post('/accept', friendController.acceptRequest);
router.post('/reject', friendController.rejectRequest);
router.get('/list', friendController.getFriends);
router.get('/pending', friendController.getPendingRequests);
router.delete('/:friendId', friendController.removeFriend);

export default router;
