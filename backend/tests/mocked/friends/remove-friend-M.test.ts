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

  // Ensure mocks and call counts are cleared before each test so spies
  // don't accumulate call counts across tests.
  beforeEach(() => {
    jest.clearAllMocks();
  });

  afterAll(() => {
    jest.restoreAllMocks();
  });

  // Mocked behavior: Database connection error when removing friend
  // Input: Valid friend ID, database connection fails
  // Expected behavior: Returns 500 error
  // Expected output: 500 status with error message
  test('Returns 500 when database connection error occurs', async () => {
    jest
      .spyOn(friendModel, 'removeFriend')
      .mockRejectedValueOnce(new Error('Database connection failed'));

    const response = await request(app)
      .delete(`/api/friends/${friendId}`)
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(500);
    expect(friendModel.removeFriend).toHaveBeenCalledTimes(1);
    expect(friendModel.removeFriend).toHaveBeenCalledWith(testUserId, friendId);
  });

  // Mocked behavior: Database timeout when removing friend
  // Input: Valid friend ID, database operation times out
  // Expected behavior: Returns 500 error
  // Expected output: 500 status with error message
  test('Returns 500 when database timeout occurs', async () => {
    jest
      .spyOn(friendModel, 'removeFriend')
      .mockRejectedValueOnce(new Error('Operation timed out'));

    const response = await request(app)
      .delete(`/api/friends/${friendId}`)
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(500);
    expect(friendModel.removeFriend).toHaveBeenCalledTimes(1);
    expect(friendModel.removeFriend).toHaveBeenCalledWith(testUserId, friendId);
  });
});
