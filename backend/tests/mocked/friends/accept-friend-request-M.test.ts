import {
  describe,
  expect,
  test,
  jest,
  beforeAll,
  beforeEach,
  afterAll,
} from '@jest/globals';
import dotenv from 'dotenv';
import request from 'supertest';
import express from 'express';
import router from '../../../src/routes/routes';
import mongoose from 'mongoose';
import jwt from 'jsonwebtoken';
import { userModel } from '../../../src/models/user.model';
import { friendModel, FriendRequest } from '../../../src/models/friends.model';
import path from 'path';

// Load test environment variables
dotenv.config({ path: path.resolve(__dirname, '../../../.env.test') });

// Create Express app for testing
const app = express();
app.use(express.json());
app.use('/api', router);

// Interface POST /api/friends/accept
describe('Mocked POST /api/friends/accept', () => {
  let authToken: string;
  let testUserId: string;
  let pendingRequestId: string;

  beforeAll(() => {
    // Silence console.error during tests
    jest.spyOn(console, 'error').mockImplementation(() => {});

    testUserId = new mongoose.Types.ObjectId().toString();
    pendingRequestId = new mongoose.Types.ObjectId().toString();

    authToken = jwt.sign(
      { id: testUserId },
      process.env.JWT_SECRET || 'test-secret'
    );

    // Mock userModel.findById (required by auth middleware)
    jest.spyOn(userModel, 'findById').mockImplementation(async (id: any) => {
      return {
        _id: id,
        googleId: 'mock-google-id',
        email: 'mock@example.com',
        name: 'Mock User',
      } as any;
    });
  });

  beforeEach(() => {
    // Clear mock call history between tests
    jest.clearAllMocks();
  });

  afterAll(() => {
    jest.restoreAllMocks();
  });

  // Mocked behavior: Database error when accepting
  // Input: Valid request ID
  // Expected behavior: Returns 500 error
  // Expected output: 500 status
  // Note: This tests error handling that can't be provoked in unmocked tests
  test('Returns 500 when database error occurs', async () => {
    // Mock friendModel.acceptRequest to throw error
    jest
      .spyOn(friendModel, 'acceptRequest')
      .mockRejectedValueOnce(new Error('Database connection failed'));

    const response = await request(app)
      .post('/api/friends/accept')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ requestId: pendingRequestId });

    expect(response.status).toBe(500);
    expect(friendModel.acceptRequest).toHaveBeenCalledTimes(1);
    expect(friendModel.acceptRequest).toHaveBeenCalledWith(pendingRequestId);
  });

  // Mocked behavior: Database timeout error
  // Input: Valid request ID
  // Expected behavior: Returns 500 error
  // Expected output: 500 status
  test('Returns 500 when database timeout occurs', async () => {
    // Mock friendModel.acceptRequest to throw timeout error
    jest
      .spyOn(friendModel, 'acceptRequest')
      .mockRejectedValueOnce(new Error('Query timeout'));

    const response = await request(app)
      .post('/api/friends/accept')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ requestId: pendingRequestId });

    expect(response.status).toBe(500);
    expect(friendModel.acceptRequest).toHaveBeenCalledTimes(1);
    expect(friendModel.acceptRequest).toHaveBeenCalledWith(pendingRequestId);
  });
});
