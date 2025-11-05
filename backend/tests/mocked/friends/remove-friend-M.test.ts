import {
  describe,
  expect,
  test,
  jest,
  beforeAll,
  afterAll,
} from '@jest/globals';
import dotenv from 'dotenv';
import request from 'supertest';
import express from 'express';
import router from '../../../src/routes/routes';
import mongoose from 'mongoose';
import jwt from 'jsonwebtoken';
import { userModel } from '../../../src/models/user.model';
import { friendModel } from '../../../src/models/friends.model';
import path from 'path';

// Load test environment variables
dotenv.config({ path: path.resolve(__dirname, '../../../.env.test') });

// Create Express app for testing
const app = express();
app.use(express.json());
app.use('/api', router);

// Interface DELETE /api/friends/:friendId
describe('Mocked DELETE /api/friends/:friendId', () => {
  let authToken: string;
  let testUserId: string;
  let friendId: string;

  beforeAll(() => {
    // Silence console.error during tests
    jest.spyOn(console, 'error').mockImplementation(() => {});

    testUserId = new mongoose.Types.ObjectId().toString();
    friendId = new mongoose.Types.ObjectId().toString();

    authToken = jwt.sign(
      { id: testUserId },
      process.env.JWT_SECRET || 'test-secret'
    );

    // Mock userModel.findById
    jest.spyOn(userModel, 'findById').mockImplementation(async (id: any) => {
      return {
        _id: id,
        googleId: 'mock-google-id',
        email: 'mock@example.com',
        name: 'Mock User',
      } as any;
    });
  });

  afterAll(() => {
    jest.restoreAllMocks();
  });

  // Mocked behavior: Successfully remove friend
  // Input: Valid friend ID
  // Expected behavior: Deletes friend relationship
  // Expected output: 200 status, success message
  test('Successfully removes friend', async () => {
    // Mock friendModel.removeFriend
    jest.spyOn(friendModel, 'removeFriend').mockResolvedValueOnce({
      _id: new mongoose.Types.ObjectId(),
      sender: testUserId,
      receiver: friendId,
      status: 'accepted',
    } as any);

    const response = await request(app)
      .delete(`/api/friends/${friendId}`)
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty(
      'message',
      'Friend removed successfully'
    );
  });

  // Mocked behavior: Remove friend without authentication
  // Input: No auth token
  // Expected behavior: Returns 401 unauthorized
  // Expected output: 401 status
  test('Returns 401 when not authenticated', async () => {
    const response = await request(app).delete(`/api/friends/${friendId}`);

    expect(response.status).toBe(401);
  });

  // Mocked behavior: Remove non-existent friend
  // Input: Invalid friend ID
  // Expected behavior: Handles gracefully
  // Expected output: Success or error status
  test('Handles removing non-existent friend', async () => {
    // Mock friendModel.removeFriend to return null
    jest.spyOn(friendModel, 'removeFriend').mockResolvedValueOnce(null);

    const response = await request(app)
      .delete(`/api/friends/${friendId}`)
      .set('Authorization', `Bearer ${authToken}`);

    // Should still return 200 even if friend doesn't exist
    expect(response.status).toBe(200);
  });

  // Mocked behavior: Database error when removing friend
  // Input: Valid friend ID
  // Expected behavior: Returns 500 error
  // Expected output: 500 status
  test('Returns 500 when database error occurs', async () => {
    // Mock friendModel.removeFriend to throw error
    jest
      .spyOn(friendModel, 'removeFriend')
      .mockRejectedValueOnce(new Error('Database error'));

    const response = await request(app)
      .delete(`/api/friends/${friendId}`)
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(500);
  });
});
