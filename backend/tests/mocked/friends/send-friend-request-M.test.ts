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

// Interface POST /api/friends/request
describe('Mocked POST /api/friends/request', () => {
  let authToken: string;
  let testUserId: string;
  let receiverUserId: string;
  let receiverFriendCode: string;

  beforeAll(() => {
    // Silence console.error during tests
    jest.spyOn(console, 'error').mockImplementation(() => {});

    // Create fake user IDs
    testUserId = new mongoose.Types.ObjectId().toString();
    receiverUserId = new mongoose.Types.ObjectId().toString();
    receiverFriendCode = 'TEST123456';

    authToken = jwt.sign(
      { id: testUserId },
      process.env.JWT_SECRET || 'test-secret'
    );

    // Mock userModel.findById to return a fake user (for auth middleware)
    jest.spyOn(userModel, 'findById').mockImplementation(async (id: any) => {
      return {
        _id: id,
        googleId: 'mock-google-id',
        email: 'mock@example.com',
        name: 'Mock User',
        friendCode: 'SENDER123',
      } as any;
    });
  });

  afterAll(() => {
    jest.restoreAllMocks();
  });

  // Mocked behavior: Successfully send friend request
  // Input: Valid receiver friend code
  // Expected behavior: Creates friend request with pending status
  // Expected output: 201 status, friend request object
  test('Successfully sends friend request', async () => {
    // Mock userModel.findByFriendCode to return receiver
    jest.spyOn(userModel, 'findByFriendCode').mockResolvedValueOnce({
      _id: new mongoose.Types.ObjectId(receiverUserId),
      googleId: 'receiver-google-id',
      email: 'receiver@example.com',
      name: 'Receiver User',
      friendCode: receiverFriendCode,
    } as any);

    // Mock friendModel.sendRequest to return created request
    jest.spyOn(friendModel, 'sendRequest').mockResolvedValueOnce({
      _id: new mongoose.Types.ObjectId(),
      sender: testUserId,
      receiver: receiverUserId,
      status: 'pending',
    } as any);

    const response = await request(app)
      .post('/api/friends/request')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ receiverCode: receiverFriendCode });

    expect(response.status).toBe(201);
    expect(response.body).toHaveProperty('message', 'Friend request sent');
    expect(response.body.data).toHaveProperty('sender', testUserId);
    expect(response.body.data).toHaveProperty('receiver', receiverUserId);
    expect(response.body.data).toHaveProperty('status', 'pending');
  });

  // Mocked behavior: Send request with non-existent friend code
  // Input: Invalid friend code
  // Expected behavior: Returns 404 error
  // Expected output: 404 status, error message
  test('Returns 404 when receiver not found', async () => {
    // Mock findByFriendCode to return null
    jest.spyOn(userModel, 'findByFriendCode').mockResolvedValueOnce(null);

    const response = await request(app)
      .post('/api/friends/request')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ receiverCode: 'INVALID_CODE_12345' });

    expect(response.status).toBe(404);
    expect(response.body).toHaveProperty('message', 'User not found');
  });

  // Mocked behavior: Send request without authentication
  // Input: No auth token
  // Expected behavior: Returns 401 unauthorized
  // Expected output: 401 status
  test('Returns 401 when not authenticated', async () => {
    const response = await request(app)
      .post('/api/friends/request')
      .send({ receiverCode: receiverFriendCode });

    expect(response.status).toBe(401);
  });

  // Mocked behavior: Send request with missing receiverCode
  // Input: Empty body
  // Expected behavior: Returns 400 validation error
  // Expected output: 400 status
  test('Returns 400 when receiverCode is missing', async () => {
    const response = await request(app)
      .post('/api/friends/request')
      .set('Authorization', `Bearer ${authToken}`)
      .send({});

    expect(response.status).toBe(400);
  });

  // Mocked behavior: Send request with empty receiverCode
  // Input: Empty string receiverCode
  // Expected behavior: Returns 400 validation error
  // Expected output: 400 status
  test('Returns 400 when receiverCode is empty', async () => {
    const response = await request(app)
      .post('/api/friends/request')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ receiverCode: '' });

    expect(response.status).toBe(400);
  });

  // Mocked behavior: Database error when sending request
  // Input: Valid friend code
  // Expected behavior: Returns 500 error
  // Expected output: 500 status
  test('Returns 500 when database error occurs', async () => {
    // Mock userModel.findByFriendCode to return receiver
    jest.spyOn(userModel, 'findByFriendCode').mockResolvedValueOnce({
      _id: new mongoose.Types.ObjectId(receiverUserId),
      googleId: 'receiver-google-id',
      email: 'receiver@example.com',
      name: 'Receiver User',
      friendCode: receiverFriendCode,
    } as any);

    // Mock friendModel.sendRequest to throw error
    jest
      .spyOn(friendModel, 'sendRequest')
      .mockRejectedValueOnce(new Error('Database error'));

    const response = await request(app)
      .post('/api/friends/request')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ receiverCode: receiverFriendCode });

    expect(response.status).toBe(500);
  });
});
