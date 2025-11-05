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

// Interface POST /api/friends/reject
describe('Mocked POST /api/friends/reject', () => {
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

  // Mocked behavior: Successfully reject friend request
  // Input: Valid pending request ID
  // Expected behavior: Updates request status to rejected
  // Expected output: 200 status, updated request
  test('Successfully rejects friend request', async () => {
    // Mock friendModel.rejectRequest
    jest.spyOn(friendModel, 'rejectRequest').mockResolvedValueOnce({
      _id: pendingRequestId,
      sender: new mongoose.Types.ObjectId(),
      receiver: testUserId,
      status: 'rejected',
    } as any);

    const response = await request(app)
      .post('/api/friends/reject')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ requestId: pendingRequestId });

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('message', 'Friend request rejected');
    expect(response.body.data).toHaveProperty('status', 'rejected');
  });

  // Mocked behavior: Reject request without authentication
  // Input: No auth token
  // Expected behavior: Returns 401 unauthorized
  // Expected output: 401 status
  test('Returns 401 when not authenticated', async () => {
    const response = await request(app)
      .post('/api/friends/reject')
      .send({ requestId: pendingRequestId });

    expect(response.status).toBe(401);
  });

  // Mocked behavior: Reject with missing requestId
  // Input: Empty body
  // Expected behavior: Returns 400 validation error
  // Expected output: 400 status with error message
  test('Returns 400 when requestId is missing', async () => {
    const response = await request(app)
      .post('/api/friends/reject')
      .set('Authorization', `Bearer ${authToken}`)
      .send({});

    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('message', 'Request ID is required');
  });

  // Mocked behavior: Database error when rejecting
  // Input: Valid request ID
  // Expected behavior: Returns 500 error
  // Expected output: 500 status
  test('Returns 500 when database error occurs', async () => {
    // Mock friendModel.rejectRequest to throw error
    jest
      .spyOn(friendModel, 'rejectRequest')
      .mockRejectedValueOnce(new Error('Database error'));

    const response = await request(app)
      .post('/api/friends/reject')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ requestId: pendingRequestId });

    expect(response.status).toBe(500);
  });
});
