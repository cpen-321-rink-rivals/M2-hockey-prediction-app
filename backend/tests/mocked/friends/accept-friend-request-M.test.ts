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

  // Mocked behavior: Successfully accept friend request
  // Input: Valid pending request ID
  // Expected behavior: Updates request status to accepted
  // Expected output: 200 status, updated request
  test('Successfully accepts friend request', async () => {
    // Mock friendModel.acceptRequest
    jest.spyOn(friendModel, 'acceptRequest').mockResolvedValueOnce({
      _id: pendingRequestId,
      sender: new mongoose.Types.ObjectId(),
      receiver: testUserId,
      status: 'accepted',
    } as any);

    const response = await request(app)
      .post('/api/friends/accept')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ requestId: pendingRequestId });

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('message', 'Friend request accepted');
    expect(response.body.data).toHaveProperty('status', 'accepted');
  });

  // Mocked behavior: Accept request without authentication
  // Input: No auth token
  // Expected behavior: Returns 401 unauthorized
  // Expected output: 401 status
  test('Returns 401 when not authenticated', async () => {
    const response = await request(app)
      .post('/api/friends/accept')
      .send({ requestId: pendingRequestId });

    expect(response.status).toBe(401);
  });

  // Mocked behavior: Accept with missing requestId
  // Input: Empty body
  // Expected behavior: Returns 400 validation error
  // Expected output: 400 status with error message
  test('Returns 400 when requestId is missing', async () => {
    const response = await request(app)
      .post('/api/friends/accept')
      .set('Authorization', `Bearer ${authToken}`)
      .send({});

    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('message', 'Request ID is required');
  });

  // Mocked behavior: Database error when accepting
  // Input: Valid request ID
  // Expected behavior: Returns 500 error
  // Expected output: 500 status
  test('Returns 500 when database error occurs', async () => {
    // Mock friendModel.acceptRequest to throw error
    jest
      .spyOn(friendModel, 'acceptRequest')
      .mockRejectedValueOnce(new Error('Database error'));

    const response = await request(app)
      .post('/api/friends/accept')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ requestId: pendingRequestId });

    expect(response.status).toBe(500);
  });
});
