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

  afterAll(() => {
    jest.restoreAllMocks();
  });

  // Mocked behavior: Successfully get friends list
  // Input: Authenticated user
  // Expected behavior: Returns list of accepted friend requests
  // Expected output: 200 status, array of friends
  test('Successfully retrieves friends list', async () => {
    const mockFriends = [
      {
        _id: new mongoose.Types.ObjectId(),
        sender: {
          _id: testUserId,
          name: 'Test User',
          email: 'test@example.com',
        },
        receiver: {
          _id: new mongoose.Types.ObjectId(),
          name: 'Friend 1',
          email: 'friend1@example.com',
        },
        status: 'accepted',
      },
      {
        _id: new mongoose.Types.ObjectId(),
        sender: {
          _id: new mongoose.Types.ObjectId(),
          name: 'Friend 2',
          email: 'friend2@example.com',
        },
        receiver: {
          _id: testUserId,
          name: 'Test User',
          email: 'test@example.com',
        },
        status: 'accepted',
      },
    ];

    // Mock friendModel.getFriends
    jest
      .spyOn(friendModel, 'getFriends')
      .mockResolvedValueOnce(mockFriends as any);

    const response = await request(app)
      .get('/api/friends/list')
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty(
      'message',
      'Friends fetched successfully'
    );
    expect(response.body.data).toBeInstanceOf(Array);
    expect(response.body.data).toHaveLength(2);
  });

  // Mocked behavior: Get friends list with no friends
  // Input: Authenticated user with no friends
  // Expected behavior: Returns empty array
  // Expected output: 200 status, empty array
  test('Returns empty array when user has no friends', async () => {
    // Mock friendModel.getFriends to return empty array
    jest.spyOn(friendModel, 'getFriends').mockResolvedValueOnce([]);

    const response = await request(app)
      .get('/api/friends/list')
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(200);
    expect(response.body.data).toBeInstanceOf(Array);
    expect(response.body.data).toHaveLength(0);
  });

  // Mocked behavior: Get friends without authentication
  // Input: No auth token
  // Expected behavior: Returns 401 unauthorized
  // Expected output: 401 status
  test('Returns 401 when not authenticated', async () => {
    const response = await request(app).get('/api/friends/list');

    expect(response.status).toBe(401);
  });

  // Mocked behavior: Database error when fetching friends
  // Input: Authenticated user
  // Expected behavior: Returns 500 error
  // Expected output: 500 status
  test('Returns 500 when database error occurs', async () => {
    // Mock friendModel.getFriends to throw error
    jest
      .spyOn(friendModel, 'getFriends')
      .mockRejectedValueOnce(new Error('Database error'));

    const response = await request(app)
      .get('/api/friends/list')
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(500);
  });
});
