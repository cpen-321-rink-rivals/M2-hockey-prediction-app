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

// Interface GET /api/friends/list
describe('Mocked GET /api/friends/list', () => {
  let authToken: string;
  let testUserId: string;

  beforeAll(() => {
    // Clear mock call history before the test suite (ensure a clean start)
    jest.clearAllMocks();

    // Silence console.error during tests
    jest.spyOn(console, 'error').mockImplementation(() => {});

    testUserId = new mongoose.Types.ObjectId().toString();

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

  // Mocked behavior: Database error when fetching friends
  // Input: Authenticated user
  // Expected behavior: Returns 500 error
  // Expected output: 500 status
  // Note: This tests error handling that can't be provoked in unmocked tests
  test('Returns 500 when database error occurs', async () => {
    // Mock friendModel.getFriends to throw error
    jest
      .spyOn(friendModel, 'getFriends')
      .mockRejectedValueOnce(new Error('Database connection failed'));

    const response = await request(app)
      .get('/api/friends/list')
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(500);
    expect(friendModel.getFriends).toHaveBeenCalledTimes(1);
    expect(friendModel.getFriends).toHaveBeenCalledWith(testUserId);
  });

  // Mocked behavior: Database timeout when fetching friends
  // Input: Authenticated user
  // Expected behavior: Returns 500 error
  // Expected output: 500 status
  test('Returns 500 when database timeout occurs', async () => {
    // Mock friendModel.getFriends to throw timeout error
    jest
      .spyOn(friendModel, 'getFriends')
      .mockRejectedValueOnce(new Error('Query timeout'));

    const response = await request(app)
      .get('/api/friends/list')
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(500);
    expect(friendModel.getFriends).toHaveBeenCalledTimes(1);
    expect(friendModel.getFriends).toHaveBeenCalledWith(testUserId);
  });
});
