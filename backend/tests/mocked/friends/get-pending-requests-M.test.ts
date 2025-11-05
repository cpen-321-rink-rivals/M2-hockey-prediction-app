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

// Interface GET /api/friends/pending
describe('Mocked GET /api/friends/pending', () => {
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

  // Mocked behavior: Successfully get pending requests
  // Input: Authenticated user
  // Expected behavior: Returns list of pending friend requests
  // Expected output: 200 status, array of pending requests
  test('Successfully retrieves pending friend requests', async () => {
    const mockPendingRequests = [
      {
        _id: new mongoose.Types.ObjectId(),
        sender: {
          _id: new mongoose.Types.ObjectId(),
          name: 'Sender 1',
          email: 'sender1@example.com',
        },
        receiver: testUserId,
        status: 'pending',
      },
      {
        _id: new mongoose.Types.ObjectId(),
        sender: {
          _id: new mongoose.Types.ObjectId(),
          name: 'Sender 2',
          email: 'sender2@example.com',
        },
        receiver: testUserId,
        status: 'pending',
      },
    ];

    // Mock friendModel.getPendingRequests
    jest
      .spyOn(friendModel, 'getPendingRequests')
      .mockResolvedValueOnce(mockPendingRequests as any);

    const response = await request(app)
      .get('/api/friends/pending')
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty(
      'message',
      'Pending requests fetched successfully'
    );
    expect(response.body.data).toBeInstanceOf(Array);
    expect(response.body.data).toHaveLength(2);
  });

  // Mocked behavior: Get pending requests with no pending requests
  // Input: Authenticated user with no pending requests
  // Expected behavior: Returns empty array
  // Expected output: 200 status, empty array
  test('Returns empty array when no pending requests', async () => {
    // Mock friendModel.getPendingRequests to return empty array
    jest.spyOn(friendModel, 'getPendingRequests').mockResolvedValueOnce([]);

    const response = await request(app)
      .get('/api/friends/pending')
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(200);
    expect(response.body.data).toBeInstanceOf(Array);
    expect(response.body.data).toHaveLength(0);
  });

  // Mocked behavior: Get pending requests without authentication
  // Input: No auth token
  // Expected behavior: Returns 401 unauthorized
  // Expected output: 401 status
  test('Returns 401 when not authenticated', async () => {
    const response = await request(app).get('/api/friends/pending');

    expect(response.status).toBe(401);
  });

  // Mocked behavior: Database error when fetching pending requests
  // Input: Authenticated user
  // Expected behavior: Returns 500 error
  // Expected output: 500 status
  test('Returns 500 when database error occurs', async () => {
    // Mock friendModel.getPendingRequests to throw error
    jest
      .spyOn(friendModel, 'getPendingRequests')
      .mockRejectedValueOnce(new Error('Database error'));

    const response = await request(app)
      .get('/api/friends/pending')
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(500);
  });
});
