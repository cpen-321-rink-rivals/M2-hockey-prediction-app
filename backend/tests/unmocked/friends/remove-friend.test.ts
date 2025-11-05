import {
  describe,
  expect,
  test,
  beforeAll,
  afterAll,
  beforeEach,
} from '@jest/globals';
import dotenv from 'dotenv';
import request from 'supertest';
import express from 'express';
import router from '../../../src/routes/routes';
import mongoose from 'mongoose';
import jwt from 'jsonwebtoken';
import { userModel } from '../../../src/models/user.model';
import { FriendRequest } from '../../../src/models/friends.model';
import { connectDB } from '../../../src/database';
import path from 'path';

// Load test environment variables
dotenv.config({ path: path.resolve(__dirname, '../../../.env.test') });

// Create Express app for testing
const app = express();
app.use(express.json());
app.use('/api', router);

// Interface DELETE /api/friends/:friendId (Integration)
describe('Unmocked DELETE /api/friends/:friendId', () => {
  let authToken: string;
  let testUserId: string;
  let friendId: string;
  let friendRequestId: string;

  beforeAll(async () => {
    await connectDB();

    // Create test user
    const testUser = await userModel.create({
      email: 'user-remove@test.com',
      name: 'Test User',
      googleId: 'google-user-remove',
    });
    testUserId = testUser._id.toString();

    // Create friend user
    const friend = await userModel.create({
      email: 'friend-remove@test.com',
      name: 'Friend User',
      googleId: 'google-friend-remove',
    });
    friendId = friend._id.toString();

    // Generate auth token
    authToken = jwt.sign({ id: testUserId }, process.env.JWT_SECRET!, {
      expiresIn: '1h',
    });
  });

  beforeEach(async () => {
    // Create an accepted friend request before each test
    const friendRequest = await FriendRequest.create({
      sender: testUserId,
      receiver: friendId,
      status: 'accepted',
    });
    friendRequestId = (friendRequest as any)._id.toString();
  });

  afterEach(async () => {
    // Clean up friend requests after each test
    await FriendRequest.deleteMany({});
  });

  afterAll(async () => {
    // Cleanup
    await userModel.delete(new mongoose.Types.ObjectId(testUserId));
    await userModel.delete(new mongoose.Types.ObjectId(friendId));
    await mongoose.connection.close();
  });

  // Integration test: Successfully remove friend
  // Input: Valid friend ID
  // Expected behavior: Deletes friend relationship from database
  // Expected output: 200 status, success message
  test('Successfully removes friend from database', async () => {
    const response = await request(app)
      .delete(`/api/friends/${friendId}`)
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty(
      'message',
      'Friend removed successfully'
    );

    // Verify friend request was actually deleted from database
    const deletedRequest = await FriendRequest.findById(friendRequestId);
    expect(deletedRequest).toBeNull();
  });

  // Integration test: Remove friend where user is receiver
  // Input: Friend ID where user is receiver
  // Expected behavior: Deletes friend relationship
  // Expected output: 200 status
  test('Successfully removes friend when user is receiver', async () => {
    // Create request where test user is receiver
    await FriendRequest.deleteMany({}); // Clear previous request
    await FriendRequest.create({
      sender: friendId,
      receiver: testUserId,
      status: 'accepted',
    });

    const response = await request(app)
      .delete(`/api/friends/${friendId}`)
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty(
      'message',
      'Friend removed successfully'
    );

    // Verify it was deleted
    const remainingRequests = await FriendRequest.find({
      $or: [
        { sender: testUserId, receiver: friendId },
        { sender: friendId, receiver: testUserId },
      ],
      status: 'accepted',
    });
    expect(remainingRequests).toHaveLength(0);
  });

  // Integration test: Remove friend without authentication
  // Input: No auth token
  // Expected behavior: Returns 401 unauthorized
  // Expected output: 401 status
  test('Returns 401 when not authenticated', async () => {
    const response = await request(app).delete(`/api/friends/${friendId}`);

    expect(response.status).toBe(401);

    // Verify friend wasn't removed (request should still exist)
    const stillExists = await FriendRequest.findOne({
      $or: [
        { sender: testUserId, receiver: friendId },
        { sender: friendId, receiver: testUserId },
      ],
      status: 'accepted',
    });
    expect(stillExists).not.toBeNull();
  });

  // Integration test: Remove non-existent friend
  // Input: Invalid friend ID
  // Expected behavior: Handles gracefully
  // Expected output: 200 status (idempotent)
  test('Handles removing non-existent friend gracefully', async () => {
    const fakeId = new mongoose.Types.ObjectId().toString();

    const response = await request(app)
      .delete(`/api/friends/${fakeId}`)
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(200);
  });

  // Integration test: Remove pending request (not accepted)
  // Input: Friend ID with pending status
  // Expected behavior: Does not remove pending request
  // Expected output: 200 status
  test('Does not remove pending friend requests', async () => {
    // Create pending request
    await FriendRequest.deleteMany({}); // Clear accepted request
    const pendingRequest = await FriendRequest.create({
      sender: testUserId,
      receiver: friendId,
      status: 'pending',
    });

    const response = await request(app)
      .delete(`/api/friends/${friendId}`)
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(200);

    // Verify pending request still exists (only accepted are removed)
    const stillPending = await FriendRequest.findById(pendingRequest._id);
    expect(stillPending).not.toBeNull();
    expect(stillPending!.status).toBe('pending');
  });

  // Integration test: Remove friend and verify friend list updates
  // Input: Valid friend ID
  // Expected behavior: Friend no longer appears in friends list
  // Expected output: Friend removed from getFriends result
  test('Removed friend no longer appears in friends list', async () => {
    // First verify friend is in list
    const beforeResponse = await request(app)
      .get('/api/friends/list')
      .set('Authorization', `Bearer ${authToken}`);

    expect(beforeResponse.body.data).toHaveLength(1);

    // Remove friend
    await request(app)
      .delete(`/api/friends/${friendId}`)
      .set('Authorization', `Bearer ${authToken}`);

    // Verify friend is no longer in list
    const afterResponse = await request(app)
      .get('/api/friends/list')
      .set('Authorization', `Bearer ${authToken}`);

    expect(afterResponse.body.data).toHaveLength(0);
  });
});
